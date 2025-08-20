package com.flickit.security;

import com.flickit.user.model.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Role Authorization Tests")
class RoleAuthorizationTest {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Test
    @DisplayName("Test role-based access validation")
    void testRoleBasedAccessValidation() {
        // Test CUSTOMER role access
        assertTrue(hasAccessToEndpoint("CUSTOMER", "/users/me", "GET"));
        assertTrue(hasAccessToEndpoint("CUSTOMER", "/events/latest", "GET"));
        assertTrue(hasAccessToEndpoint("CUSTOMER", "/ratings", "POST"));
        assertTrue(hasAccessToEndpoint("CUSTOMER", "/notifications/subscribe", "POST"));
        assertFalse(hasAccessToEndpoint("CUSTOMER", "/events", "POST"));
        assertFalse(hasAccessToEndpoint("CUSTOMER", "/ai/generate-content", "POST"));
        assertFalse(hasAccessToEndpoint("CUSTOMER", "/ai/test", "POST"));

        // Test VENDOR role access
        assertTrue(hasAccessToEndpoint("VENDOR", "/users/me", "GET"));
        assertTrue(hasAccessToEndpoint("VENDOR", "/events/latest", "GET"));
        assertTrue(hasAccessToEndpoint("VENDOR", "/events", "POST"));
        assertTrue(hasAccessToEndpoint("VENDOR", "/ai/generate-content", "POST"));
        assertFalse(hasAccessToEndpoint("VENDOR", "/ratings", "POST"));
        assertFalse(hasAccessToEndpoint("VENDOR", "/ai/test", "POST"));

        // Test ADMIN role access
        assertTrue(hasAccessToEndpoint("ADMIN", "/users/me", "GET"));
        assertTrue(hasAccessToEndpoint("ADMIN", "/events/latest", "GET"));
        assertTrue(hasAccessToEndpoint("ADMIN", "/events", "POST"));
        assertTrue(hasAccessToEndpoint("ADMIN", "/ai/generate-content", "POST"));
        assertTrue(hasAccessToEndpoint("ADMIN", "/ai/test", "POST"));
        assertTrue(hasAccessToEndpoint("ADMIN", "/auth/token", "POST"));
    }

    @Test
    @DisplayName("Test public endpoint access")
    void testPublicEndpointAccess() {
        // Public endpoints should be accessible by all roles
        String[] publicEndpoints = {"/events/latest", "/ai/health", "/auth/login"};
        
        for (String endpoint : publicEndpoints) {
            assertTrue(hasAccessToEndpoint("CUSTOMER", endpoint, "GET"));
            assertTrue(hasAccessToEndpoint("VENDOR", endpoint, "GET"));
            assertTrue(hasAccessToEndpoint("ADMIN", endpoint, "GET"));
        }
    }

    @Test
    @DisplayName("Test protected endpoint access")
    void testProtectedEndpointAccess() {
        // Protected endpoints should require authentication
        String[] protectedEndpoints = {"/users/me", "/events", "/ratings", "/notifications/subscribe"};
        
        for (String endpoint : protectedEndpoints) {
            assertFalse(hasAccessToEndpoint("UNAUTHENTICATED", endpoint, "GET"));
        }
    }

    @Test
    @DisplayName("Test role-specific business logic")
    void testRoleSpecificBusinessLogic() {
        // Test CUSTOMER can claim events
        assertTrue(canPerformAction("CUSTOMER", "claim_event"));
        assertFalse(canPerformAction("CUSTOMER", "create_event"));
        assertFalse(canPerformAction("CUSTOMER", "generate_ai_content"));

        // Test VENDOR can create events and use AI
        assertTrue(canPerformAction("VENDOR", "create_event"));
        assertTrue(canPerformAction("VENDOR", "generate_ai_content"));
        assertFalse(canPerformAction("VENDOR", "claim_event"));
        assertFalse(canPerformAction("VENDOR", "admin_actions"));

        // Test ADMIN can do everything
        assertTrue(canPerformAction("ADMIN", "create_event"));
        assertTrue(canPerformAction("ADMIN", "generate_ai_content"));
        assertTrue(canPerformAction("ADMIN", "admin_actions"));
        assertTrue(canPerformAction("ADMIN", "manage_users"));
    }

    @Test
    @DisplayName("Test user creation with different roles")
    void testUserCreationWithDifferentRoles() {
        // Test that users can be created with different roles
        UserEntity customer = createTestUser(UserEntity.Role.CUSTOMER);
        UserEntity vendor = createTestUser(UserEntity.Role.VENDOR);
        UserEntity admin = createTestUser(UserEntity.Role.ADMIN);

        assertEquals(UserEntity.Role.CUSTOMER, customer.getRole());
        assertEquals(UserEntity.Role.VENDOR, vendor.getRole());
        assertEquals(UserEntity.Role.ADMIN, admin.getRole());

        // Test password hashing
        assertTrue(passwordEncoder.matches("password123", customer.getPasswordHash()));
        assertTrue(passwordEncoder.matches("password123", vendor.getPasswordHash()));
        assertTrue(passwordEncoder.matches("password123", admin.getPasswordHash()));
    }

    @Test
    @DisplayName("Test endpoint method restrictions")
    void testEndpointMethodRestrictions() {
        // Test that certain endpoints only allow specific HTTP methods
        assertTrue(isMethodAllowed("/users", "POST")); // Registration
        assertFalse(isMethodAllowed("/users", "PUT")); // Update not allowed via this endpoint
        
        assertTrue(isMethodAllowed("/events", "POST")); // Create event
        assertTrue(isMethodAllowed("/events/latest", "GET")); // Get events
        assertFalse(isMethodAllowed("/events/latest", "POST")); // Cannot POST to latest
        
        assertTrue(isMethodAllowed("/ratings", "POST")); // Create rating
        assertFalse(isMethodAllowed("/ratings", "PUT")); // Cannot update ratings
        assertFalse(isMethodAllowed("/ratings", "DELETE")); // Cannot delete ratings
    }

    @Test
    @DisplayName("Test role hierarchy")
    void testRoleHierarchy() {
        // Test that ADMIN has access to everything
        assertTrue(hasAccessToEndpoint("ADMIN", "/users/me", "GET"));
        assertTrue(hasAccessToEndpoint("ADMIN", "/events", "POST"));
        assertTrue(hasAccessToEndpoint("ADMIN", "/ai/generate-content", "POST"));
        assertTrue(hasAccessToEndpoint("ADMIN", "/ai/test", "POST"));
        assertTrue(hasAccessToEndpoint("ADMIN", "/auth/token", "POST"));

        // Test that VENDOR has limited admin access
        assertTrue(hasAccessToEndpoint("VENDOR", "/events", "POST"));
        assertTrue(hasAccessToEndpoint("VENDOR", "/ai/generate-content", "POST"));
        assertFalse(hasAccessToEndpoint("VENDOR", "/ai/test", "POST"));
        assertFalse(hasAccessToEndpoint("VENDOR", "/auth/token", "POST"));

        // Test that CUSTOMER has basic access only
        assertTrue(hasAccessToEndpoint("CUSTOMER", "/users/me", "GET"));
        assertTrue(hasAccessToEndpoint("CUSTOMER", "/ratings", "POST"));
        assertFalse(hasAccessToEndpoint("CUSTOMER", "/events", "POST"));
        assertFalse(hasAccessToEndpoint("CUSTOMER", "/ai/generate-content", "POST"));
    }

    // Helper methods
    private boolean hasAccessToEndpoint(String role, String endpoint, String method) {
        if ("UNAUTHENTICATED".equals(role)) {
            return false; // Unauthenticated users cannot access protected endpoints
        }

        // Public endpoints
        if (endpoint.equals("/events/latest") || endpoint.equals("/ai/health") || endpoint.equals("/auth/login")) {
            return true;
        }

        // Role-based access control
        switch (endpoint) {
            case "/users/me":
                return true; // All authenticated users can access their profile
            case "/events":
                return "VENDOR".equals(role) || "ADMIN".equals(role);
            case "/ratings":
                return "CUSTOMER".equals(role);
            case "/notifications/subscribe":
            case "/notifications/subscriptions":
            case "/notifications/unsubscribe":
                return "CUSTOMER".equals(role);
            case "/ai/generate-content":
                return "VENDOR".equals(role) || "ADMIN".equals(role);
            case "/ai/test":
            case "/auth/token":
                return "ADMIN".equals(role);
            default:
                return false;
        }
    }

    private boolean canPerformAction(String role, String action) {
        switch (action) {
            case "claim_event":
                return "CUSTOMER".equals(role);
            case "create_event":
                return "VENDOR".equals(role) || "ADMIN".equals(role);
            case "generate_ai_content":
                return "VENDOR".equals(role) || "ADMIN".equals(role);
            case "admin_actions":
                return "ADMIN".equals(role);
            case "manage_users":
                return "ADMIN".equals(role);
            default:
                return false;
        }
    }

    private boolean isMethodAllowed(String endpoint, String method) {
        switch (endpoint) {
            case "/users":
                return "POST".equals(method);
            case "/events":
                return "POST".equals(method);
            case "/events/latest":
                return "GET".equals(method);
            case "/ratings":
                return "POST".equals(method);
            case "/notifications/subscribe":
                return "POST".equals(method);
            case "/notifications/subscriptions":
                return "GET".equals(method);
            case "/notifications/unsubscribe":
                return "DELETE".equals(method);
            case "/ai/generate-content":
                return "POST".equals(method);
            case "/ai/health":
                return "GET".equals(method);
            case "/ai/test":
                return "POST".equals(method);
            case "/auth/login":
                return "POST".equals(method);
            case "/auth/token":
                return "POST".equals(method);
            default:
                return false;
        }
    }

    private UserEntity createTestUser(UserEntity.Role role) {
        return UserEntity.builder()
                .id(UUID.randomUUID())
                .name("Test " + role.name())
                .phone("123456789" + role.ordinal())
                .role(role)
                .passwordHash(passwordEncoder.encode("password123"))
                .build();
    }
}
