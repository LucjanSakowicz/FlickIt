package com.flickit.rating.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flickit.auth.model.CurrentUser;
import com.flickit.auth.service.AuthContext;
import com.flickit.event.model.EventEntity;
import com.flickit.event.repository.EventRepository;
import com.flickit.rating.dto.CreateRatingRequest;
import com.flickit.rating.dto.RatingDto;
import com.flickit.rating.model.RatingEntity;
import com.flickit.rating.repository.RatingRepository;
import com.flickit.user.model.UserEntity;
import com.flickit.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class RatingServiceTest {

    @MockBean
    private RatingRepository ratingRepository;
    @MockBean
    private EventRepository eventRepository;
    @MockBean
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RatingService ratingService;

    private UUID testUserId;
    private UUID testEventId;
    private UUID testVendorId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEventId = UUID.randomUUID();
        testVendorId = UUID.randomUUID();
    }

    @Test
    void createRating_shouldSaveAndReturnRatingDto() {
        // given
        CreateRatingRequest request = new CreateRatingRequest();
        request.setEventId(testEventId);
        request.setRating(5);
        request.setComment("Great event!");

        CurrentUser currentUser = new CurrentUser(testUserId, UserEntity.Role.CUSTOMER);
        
        EventEntity event = new EventEntity();
        // EventEntity używa Lombok @Getter, więc musimy użyć refleksji lub mockować getVendorId()
        
        UserEntity vendor = new UserEntity();
        vendor.setId(testVendorId);
        vendor.setRating(4.0);
        vendor.setRatingCount(2);

        RatingEntity savedRating = RatingEntity.builder()
                .eventId(testEventId)
                .userId(testUserId)
                .rating(5)
                .comment("Great event!")
                .build();

        when(ratingRepository.existsByEventIdAndUserId(testEventId, testUserId)).thenReturn(false);
        when(ratingRepository.save(any(RatingEntity.class))).thenReturn(savedRating);
        when(eventRepository.findById(testEventId)).thenReturn(Optional.of(event));
        when(userRepository.findById(testVendorId)).thenReturn(Optional.of(vendor));
        
        // Mock getVendorId() since EventEntity only has @Getter
        EventEntity mockEvent = mock(EventEntity.class);
        when(mockEvent.getVendorId()).thenReturn(testVendorId);
        when(eventRepository.findById(testEventId)).thenReturn(Optional.of(mockEvent));

        try (MockedStatic<AuthContext> authContextMock = Mockito.mockStatic(AuthContext.class)) {
            authContextMock.when(AuthContext::getCurrentUser).thenReturn(currentUser);

            // when
            RatingDto result = ratingService.rateEvent(request);

            // then
            assertNotNull(result);
            verify(ratingRepository).save(any(RatingEntity.class));
            verify(userRepository).save(vendor);
            assertEquals(4.33, vendor.getRating(), 0.01); // (4*2 + 5) / 3 = 4.33
            assertEquals(3, vendor.getRatingCount());
        }
    }

    @Test
    void createRating_shouldThrowIfUserNotAuthenticated() {
        // given
        CreateRatingRequest request = new CreateRatingRequest();
        request.setEventId(testEventId);
        request.setRating(5);

        try (MockedStatic<AuthContext> authContextMock = Mockito.mockStatic(AuthContext.class)) {
            authContextMock.when(AuthContext::getCurrentUser).thenReturn(null);

            // when & then
            IllegalStateException exception = assertThrows(IllegalStateException.class, 
                () -> ratingService.rateEvent(request));
            assertEquals("Unauthenticated access - this should not happen.", exception.getMessage());
            verify(ratingRepository, never()).save(any());
        }
    }

    @Test
    void createRating_shouldThrowIfAlreadyRated() {
        // given
        CreateRatingRequest request = new CreateRatingRequest();
        request.setEventId(testEventId);
        request.setRating(5);

        CurrentUser currentUser = new CurrentUser(testUserId, UserEntity.Role.CUSTOMER);
        when(ratingRepository.existsByEventIdAndUserId(testEventId, testUserId)).thenReturn(true);

        try (MockedStatic<AuthContext> authContextMock = Mockito.mockStatic(AuthContext.class)) {
            authContextMock.when(AuthContext::getCurrentUser).thenReturn(currentUser);

            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> ratingService.rateEvent(request));
            assertEquals("You have already rated this event.", exception.getMessage());
            verify(ratingRepository, never()).save(any());
        }
    }
} 