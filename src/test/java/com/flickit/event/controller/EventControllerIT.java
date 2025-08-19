package com.flickit.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flickit.event.dto.CreateEventRequest;
import com.flickit.event.dto.EventDto;
import com.flickit.event.model.EventEntity;
import com.flickit.event.repository.EventRepository;
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

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EventControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createEvent_shouldCreateEventSuccessfully() throws Exception {
        // given
        String vendorToken = createVendorAndGetToken();
        
        CreateEventRequest request = new CreateEventRequest();
        request.setTitleVendor("Pizza Sale");
        request.setDescriptionVendor("50% off all pizzas!");
        request.setLat(50.0647);
        request.setLon(19.9450); // Kraków coordinates
        request.setCategory(EventEntity.Category.FOOD);
        request.setExpiresAt(Instant.now().plusSeconds(3600));
        request.setDiscount("50% off");
        request.setStyle("casual");

        // when & then
        mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + vendorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titleVendor", is("Pizza Sale")))
                .andExpect(jsonPath("$.title", is("Pizza Sale"))) // computed field
                .andExpect(jsonPath("$.titleAi", startsWith("AI: ")))
                .andExpect(jsonPath("$.lat", is(50.0647)))
                .andExpect(jsonPath("$.lon", is(19.9450)))
                .andExpect(jsonPath("$.category", is("FOOD")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.vendorId", notNullValue()));
    }

    @Test
    void createEvent_shouldFailWithoutVendorRole() throws Exception {
        // given
        String customerToken = createCustomerAndGetToken();
        
        CreateEventRequest request = new CreateEventRequest();
        request.setTitleVendor("Test Event");
        request.setLat(50.0);
        request.setLon(20.0);
        request.setCategory(EventEntity.Category.OTHER);
        request.setExpiresAt(Instant.now().plusSeconds(3600));

        // when & then
        mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getLatestEvents_shouldReturnEventsByLocation() throws Exception {
        // given
        String vendorToken = createVendorAndGetToken();
        
        // Create event in Kraków
        CreateEventRequest krakow = new CreateEventRequest();
        krakow.setTitleVendor("Kraków Event");
        krakow.setLat(50.0647);
        krakow.setLon(19.9450);
        krakow.setCategory(EventEntity.Category.SERVICE);
        krakow.setExpiresAt(Instant.now().plusSeconds(3600));

        // Create event in Warsaw (far away)
        CreateEventRequest warsaw = new CreateEventRequest();
        warsaw.setTitleVendor("Warsaw Event");
        warsaw.setLat(52.2297);
        warsaw.setLon(21.0122);
        warsaw.setCategory(EventEntity.Category.OTHER);
        warsaw.setExpiresAt(Instant.now().plusSeconds(3600));

        mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + vendorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(krakow)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + vendorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(warsaw)))
                .andExpect(status().isOk());

        // when & then - search near Kraków (should find only Kraków event)
        mockMvc.perform(get("/events/latest")
                        .param("lat", "50.0647")
                        .param("lon", "19.9450")
                        .param("radiusMeters", "5000")) // 5km radius
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titleVendor", is("Kraków Event")));
    }

    @Test
    void claimEvent_shouldClaimEventSuccessfully() throws Exception {
        // given
        String vendorToken = createVendorAndGetToken();
        String customerToken = createCustomerAndGetToken();
        
        // Create event
        CreateEventRequest request = new CreateEventRequest();
        request.setTitleVendor("Claimable Event");
        request.setLat(50.0);
        request.setLon(20.0);
        request.setCategory(EventEntity.Category.SERVICE);
        request.setExpiresAt(Instant.now().plusSeconds(3600));

        String eventResponse = mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + vendorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        EventDto event = objectMapper.readValue(eventResponse, EventDto.class);

        // when & then - claim event
        mockMvc.perform(put("/events/" + event.getId() + "/claim")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk());
    }

    @Test
    void claimEvent_shouldFailIfAlreadyClaimed() throws Exception {
        // given
        String vendorToken = createVendorAndGetToken();
        String customerToken = createCustomerAndGetToken();
        
        // Create and claim event
        CreateEventRequest request = new CreateEventRequest();
        request.setTitleVendor("Already Claimed Event");
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

        // First claim - should succeed
        mockMvc.perform(put("/events/" + event.getId() + "/claim")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk());

        // Second claim - should fail with conflict
        mockMvc.perform(put("/events/" + event.getId() + "/claim")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isConflict());
    }

    @Test
    void claimEvent_shouldFailWithoutCustomerRole() throws Exception {
        // given
        String vendorToken = createVendorAndGetToken();
        
        CreateEventRequest request = new CreateEventRequest();
        request.setTitleVendor("Test Event");
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

        // when & then - vendor trying to claim (should fail)
        mockMvc.perform(put("/events/" + event.getId() + "/claim")
                        .header("Authorization", "Bearer " + vendorToken))
                .andExpect(status().isForbidden());
    }

    private String createVendorAndGetToken() throws Exception {
        CreateUserRequest vendor = new CreateUserRequest(
                "Vendor User", "555-VENDOR", "password123", UserEntity.Role.VENDOR);
        
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vendor)))
                .andExpect(status().isOk());

        UserLoginRequest login = new UserLoginRequest();
        login.setPhone("555-VENDOR");
        login.setPassword("password123");

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }

    private String createCustomerAndGetToken() throws Exception {
        CreateUserRequest customer = new CreateUserRequest(
                "Customer User", "555-CUSTOMER", "password123", UserEntity.Role.CUSTOMER);
        
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isOk());

        UserLoginRequest login = new UserLoginRequest();
        login.setPhone("555-CUSTOMER");
        login.setPassword("password123");

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }
} 