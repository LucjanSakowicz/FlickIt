package com.flickit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flickit.user.dto.CreateUserRequest;
import com.flickit.user.dto.UserDto;
import com.flickit.user.dto.UserLoginRequest;
import com.flickit.user.model.UserEntity;
import com.flickit.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_shouldCreateUserSuccessfully() throws Exception {
        // given
        CreateUserRequest request = new CreateUserRequest(
                "Jan Kowalski",
                "123456789",
                "password123",
                UserEntity.Role.CUSTOMER
        );

        // when & then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Jan Kowalski")))
                .andExpect(jsonPath("$.phone", is("123456789")))
                .andExpect(jsonPath("$.role", is("CUSTOMER")))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.rating", is(0.0)))
                .andExpect(jsonPath("$.ratingCount", is(0)));
    }

    @Test
    void register_shouldFailForDuplicatePhone() throws Exception {
        // given
        CreateUserRequest request1 = new CreateUserRequest(
                "Jan Kowalski",
                "123456789",
                "password123",
                UserEntity.Role.CUSTOMER
        );
        CreateUserRequest request2 = new CreateUserRequest(
                "Anna Nowak",
                "123456789", // same phone
                "password456",
                UserEntity.Role.VENDOR
        );

        // when
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        // then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldFailForInvalidData() throws Exception {
        // given
        CreateUserRequest request = new CreateUserRequest(
                "", // empty name
                "123456789",
                "password123",
                UserEntity.Role.CUSTOMER
        );

        // when & then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturnTokenForValidCredentials() throws Exception {
        // given
        CreateUserRequest registerRequest = new CreateUserRequest(
                "Jan Kowalski",
                "123456789",
                "password123",
                UserEntity.Role.CUSTOMER
        );

        // Register user first
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setPhone("123456789");
        loginRequest.setPassword("password123");

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    void login_shouldFailForInvalidCredentials() throws Exception {
        // given
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setPhone("999999999");
        loginRequest.setPassword("wrongpassword");

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSelf_shouldReturnUserProfileWithValidToken() throws Exception {
        // given
        CreateUserRequest registerRequest = new CreateUserRequest(
                "Jan Kowalski",
                "123456789",
                "password123",
                UserEntity.Role.CUSTOMER
        );

        // Register user
        String registerResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserDto userDto = objectMapper.readValue(registerResponse, UserDto.class);

        // Login to get token
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setPhone("123456789");
        loginRequest.setPassword("password123");

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("token").asText();

        // when & then
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Jan Kowalski")))
                .andExpect(jsonPath("$.phone", is("123456789")))
                .andExpect(jsonPath("$.role", is("CUSTOMER")))
                .andExpect(jsonPath("$.id", is(userDto.getId().toString())));
    }

    @Test
    void getSelf_shouldFailWithoutToken() throws Exception {
        // given & when & then
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isForbidden()); // Spring Security returns 403 for unauthorized access
    }
} 