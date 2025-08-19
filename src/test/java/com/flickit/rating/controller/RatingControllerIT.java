package com.flickit.rating.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flickit.event.dto.CreateEventRequest;
import com.flickit.event.dto.EventDto;
import com.flickit.event.model.EventEntity;
import com.flickit.user.dto.CreateUserRequest;
import com.flickit.user.dto.UserLoginRequest;
import com.flickit.user.model.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RatingControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void rate_shouldCreateRatingSuccessfully() throws Exception {
        // given
        String vendorToken = createUserAndGetToken("555-VENDOR-RATE", UserEntity.Role.VENDOR);
        String customerToken = createUserAndGetToken("555-CUSTOMER-RATE", UserEntity.Role.CUSTOMER);

        // create event as vendor
        CreateEventRequest request = new CreateEventRequest();
        request.setTitleVendor("Rate Event");
        request.setLat(50.0);
        request.setLon(20.0);
        request.setCategory(EventEntity.Category.OTHER);
        request.setExpiresAt(Instant.now().plusSeconds(3600));

        String eventResponse = mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + vendorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        EventDto event = objectMapper.readValue(eventResponse, EventDto.class);

        // when & then - rate as customer
        String body = "{\"eventId\":\"" + event.getId() + "\",\"rating\":5,\"comment\":\"ok\"}";
        mockMvc.perform(post("/ratings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.eventId", notNullValue()))
                .andExpect(jsonPath("$.userId", notNullValue()));
    }

    private String createUserAndGetToken(String phone, UserEntity.Role role) throws Exception {
        CreateUserRequest user = new CreateUserRequest("Test User", phone, "password123", role);
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        UserLoginRequest login = new UserLoginRequest();
        login.setPhone(phone);
        login.setPassword("password123");

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }
}


