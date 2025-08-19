package com.flickit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flickit.auth.service.JwtService;
import com.flickit.event.dto.CreateEventRequest;
import com.flickit.user.model.UserEntity;
import com.flickit.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Role Authorization Tests")
class RoleAuthorizationTest {

    @Configuration
    static class TestConfig {
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String customerToken;
    private String vendorToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        customerToken = createUserAndGetToken(UserEntity.Role.CUSTOMER);
        vendorToken = createUserAndGetToken(UserEntity.Role.VENDOR);
        adminToken = createUserAndGetToken(UserEntity.Role.ADMIN);
    }

    @ParameterizedTest(name = "Endpoint: {0}, Method: {1}, Expected Status: {2}, Allowed Roles: {3}")
    @MethodSource("provideEndpointTestCases")
    @DisplayName("Test endpoint authorization")
    void testEndpointAuthorization(String endpoint, String method, int expectedStatus, 
                                String[] allowedRoles, String requestBody) throws Exception {
        
        // Test with each role
        testWithRole(endpoint, method, expectedStatus, customerToken, "CUSTOMER", allowedRoles, requestBody);
        testWithRole(endpoint, method, expectedStatus, vendorToken, "VENDOR", allowedRoles, requestBody);
        testWithRole(endpoint, method, expectedStatus, adminToken, "ADMIN", allowedRoles, requestBody);
    }

    private void testWithRole(String endpoint, String method, int expectedStatus, 
                            String token, String role, String[] allowedRoles, String requestBody) throws Exception {
        
        boolean isAllowed = Stream.of(allowedRoles).anyMatch(allowedRole -> 
            allowedRole.equals(role) || allowedRole.equals("ALL"));
        
        int expectedResult = isAllowed ? expectedStatus : 403; // 403 Forbidden if not allowed
        
        var requestBuilder = createRequestBuilder(method, endpoint, requestBody);
        if (token != null) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }
        
        mockMvc.perform(requestBuilder)
                .andExpect(status().is(expectedResult))
                .andDo(result -> {
                    if (result.getResponse().getStatus() != expectedResult) {
                        System.out.printf("Role: %s, Endpoint: %s %s, Expected: %d, Got: %d%n", 
                            role, method, endpoint, expectedResult, result.getResponse().getStatus());
                    }
                });
    }

    private MockHttpServletRequestBuilder createRequestBuilder(String method, String endpoint, String requestBody) {
        return switch (method.toUpperCase()) {
            case "GET" -> get(endpoint);
            case "POST" -> post(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody != null ? requestBody : "{}");
            case "PUT" -> put(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody != null ? requestBody : "{}");
            case "DELETE" -> delete(endpoint);
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        };
    }

    static Stream<Arguments> provideEndpointTestCases() {
        return Stream.of(
            // User endpoints
            Arguments.of("/users", "POST", 201, new String[]{"ALL"}, 
                "{\"name\":\"Test User\",\"phone\":\"123456789\",\"password\":\"password123\"}"),
            Arguments.of("/users/me", "GET", 200, new String[]{"CUSTOMER", "VENDOR", "ADMIN"}, null),
            
            // Event endpoints
            Arguments.of("/events", "POST", 201, new String[]{"VENDOR", "ADMIN"}, 
                "{\"titleVendor\":\"Test Event\",\"lat\":50.0,\"lon\":20.0,\"expiresAt\":\"" + 
                Instant.now().plusSeconds(3600) + "\"}"),
            Arguments.of("/events/latest", "GET", 200, new String[]{"ALL"}, null),
            Arguments.of("/events/123e4567-e89b-12d3-a456-426614174000/claim", "PUT", 200, 
                new String[]{"CUSTOMER"}, null),
            
            // Rating endpoints
            Arguments.of("/ratings", "POST", 201, new String[]{"CUSTOMER"}, 
                "{\"eventId\":\"123e4567-e89b-12d3-a456-426614174000\",\"rating\":5,\"comment\":\"Great!\"}"),
            
            // Notification endpoints
            Arguments.of("/notifications/subscribe", "POST", 200, new String[]{"CUSTOMER"}, 
                "{\"fcmToken\":\"test-token\",\"radiusMeters\":2000.0,\"latitude\":50.0,\"longitude\":20.0}"),
            Arguments.of("/notifications/subscriptions", "GET", 200, new String[]{"CUSTOMER"}, null),
            Arguments.of("/notifications/unsubscribe", "DELETE", 200, new String[]{"CUSTOMER"}, null),
            Arguments.of("/notifications/test", "POST", 200, new String[]{"ADMIN"}, null),
            
            // AI endpoints
            Arguments.of("/ai/generate-content", "POST", 200, new String[]{"VENDOR", "ADMIN"}, 
                "{\"imageUrl\":\"https://example.com/test.jpg\",\"businessType\":\"restaurant\"}"),
            Arguments.of("/ai/health", "GET", 200, new String[]{"ALL"}, null),
            Arguments.of("/ai/test", "POST", 200, new String[]{"ADMIN"}, null),
            
            // Auth endpoints
            Arguments.of("/auth/login", "POST", 200, new String[]{"ALL"}, 
                "{\"phone\":\"123456789\",\"password\":\"password123\"}"),
            Arguments.of("/auth/token", "POST", 200, new String[]{"ADMIN"}, null),
            Arguments.of("/auth/test-token", "POST", 200, new String[]{"ADMIN"}, null)
        );
    }

    @Test
    @DisplayName("Test unauthenticated access to protected endpoints")
    void testUnauthenticatedAccess() throws Exception {
        // Test that unauthenticated requests to protected endpoints return 403
        String[] protectedEndpoints = {
            "/users/me",
            "/events",
            "/ratings", 
            "/notifications/subscribe",
            "/ai/generate-content"
        };

        for (String endpoint : protectedEndpoints) {
            mockMvc.perform(get(endpoint))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("Test public endpoints accessible without authentication")
    void testPublicEndpoints() throws Exception {
        // Test that public endpoints are accessible without authentication
        String[] publicEndpoints = {
            "/events/latest",
            "/ai/health"
        };

        for (String endpoint : publicEndpoints) {
            mockMvc.perform(get(endpoint))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("Test role-specific access patterns")
    void testRoleSpecificAccess() throws Exception {
        // Test CUSTOMER can access customer-specific endpoints
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk());

        // Test VENDOR can create events
        CreateEventRequest eventRequest = new CreateEventRequest();
        eventRequest.setTitleVendor("Test Event");
        eventRequest.setLat(50.0);
        eventRequest.setLon(20.0);
        eventRequest.setExpiresAt(Instant.now().plusSeconds(3600));

        mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + vendorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isCreated());

        // Test ADMIN can access admin endpoints
        mockMvc.perform(post("/ai/test")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    private String createUserAndGetToken(UserEntity.Role role) {
        try {
            UserEntity user = UserEntity.builder()
                    .id(UUID.randomUUID())
                    .name("Test " + role.name())
                    .phone("123456789" + role.ordinal())
                    .role(role)
                    .passwordHash(passwordEncoder.encode("password123"))
                    .build();

            userRepository.save(user);
            return jwtService.generateToken(user.getId(), user.getRole().name());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test user", e);
        }
    }
}
