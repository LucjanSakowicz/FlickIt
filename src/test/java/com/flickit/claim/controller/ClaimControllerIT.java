package com.flickit.claim.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flickit.claim.dto.ClaimEventRequest;
import com.flickit.claim.dto.RateEventRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ClaimControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnOkWhenClaimingEvent() throws Exception {
        ClaimEventRequest request = new ClaimEventRequest(
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        mockMvc.perform(post("/claims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // lub .isConflict() zależnie od istniejącego wpisu
    }

    @Test
    void shouldReturnBadRequestForInvalidRating() throws Exception {
        RateEventRequest request = RateEventRequest.builder()
                .eventId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .rating(6) // nieprawidłowa ocena
                .ratedAt(Instant.now())
                .build();

        mockMvc.perform(post("/claims/rate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
