package com.flickit.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flickit.ai.dto.GenerateContentRequest;
import com.flickit.ai.service.AIService;
import com.flickit.auth.service.JwtService;
import com.flickit.user.model.UserEntity;
import com.flickit.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AIControllerIT {

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
    private AIService aiService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Mock AI service responses
        when(aiService.generateContent(any(GenerateContentRequest.class)))
                .thenReturn(com.flickit.ai.dto.GeneratedContentDto.builder()
                        .title("Test Title")
                        .description("Test Description")
                        .suggestedCategory("FOOD")
                        .confidence(0.9)
                        .modelUsed("gpt-4-vision-preview-mock")
                        .build());
    }

    @Test
    @WithMockUser(roles = "VENDOR")
    void generateContent_shouldSucceedWithVendorRole() throws Exception {
        // given
        GenerateContentRequest request = new GenerateContentRequest();
        request.setImageUrl("https://example.com/food-image.jpg");
        request.setBusinessType("restaurant");
        request.setAdditionalPrompt("Specjalna promocja na lunch");

        // when & then
        mockMvc.perform(post("/ai/generate-content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Test Title")))
                .andExpect(jsonPath("$.description", is("Test Description")))
                .andExpect(jsonPath("$.suggestedCategory", is("FOOD")))
                .andExpect(jsonPath("$.confidence", is(0.9)))
                .andExpect(jsonPath("$.modelUsed", is("gpt-4-vision-preview-mock")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void generateContent_shouldSucceedWithAdminRole() throws Exception {
        // given
        GenerateContentRequest request = new GenerateContentRequest();
        request.setImageUrl("https://example.com/service-image.jpg");
        request.setBusinessType("service");

        // when & then
        mockMvc.perform(post("/ai/generate-content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Test Title")))
                .andExpect(jsonPath("$.description", is("Test Description")));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void generateContent_shouldFailWithCustomerRole() throws Exception {
        // given
        GenerateContentRequest request = new GenerateContentRequest();
        request.setImageUrl("https://example.com/test-image.jpg");

        // when & then
        mockMvc.perform(post("/ai/generate-content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void generateContent_shouldFailWithoutToken() throws Exception {
        // given
        GenerateContentRequest request = new GenerateContentRequest();
        request.setImageUrl("https://example.com/test-image.jpg");

        // when & then
        mockMvc.perform(post("/ai/generate-content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VENDOR")
    void generateContent_shouldFailWithInvalidRequest() throws Exception {
        // given
        GenerateContentRequest request = new GenerateContentRequest();
        // Missing required imageUrl

        // when & then
        mockMvc.perform(post("/ai/generate-content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void healthCheck_shouldReturnHealthy() throws Exception {
        // when & then
        mockMvc.perform(get("/ai/health"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("healthy")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGeneration_shouldSucceedWithAdminRole() throws Exception {
        // when & then
        mockMvc.perform(post("/ai/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Test Title")))
                .andExpect(jsonPath("$.description", is("Test Description")));
    }

    @Test
    @WithMockUser(roles = "VENDOR")
    void testGeneration_shouldFailWithVendorRole() throws Exception {
        // when & then
        mockMvc.perform(post("/ai/test"))
                .andExpect(status().isForbidden());
    }
}
