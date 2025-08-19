package com.flickit.notification.service;

import com.flickit.auth.model.CurrentUser;
import com.flickit.auth.service.AuthContext;
import com.flickit.user.model.UserEntity;
import com.flickit.notification.dto.SubscribeRequest;
import com.flickit.notification.model.NotificationSubscription;
import com.flickit.notification.repository.NotificationSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationSubscriptionRepository subscriptionRepository;

    @Mock
    private FcmService fcmService;

    @InjectMocks
    private NotificationService notificationService;

    private UUID testUserId;
    private CurrentUser testUser;
    private SubscribeRequest testRequest;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = new CurrentUser(testUserId, UserEntity.Role.CUSTOMER);
        
        testRequest = new SubscribeRequest();
        testRequest.setFcmToken("test-fcm-token-123");
        testRequest.setRadiusMeters(2000.0);
        testRequest.setLatitude(50.0);
        testRequest.setLongitude(20.0);
    }

    @Test
    void subscribe_shouldCreateNewSubscription() {
        // given
        when(subscriptionRepository.findByUserIdAndFcmToken(testUserId, testRequest.getFcmToken()))
                .thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(NotificationSubscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<AuthContext> authContextMock = mockStatic(AuthContext.class)) {
            authContextMock.when(AuthContext::getCurrentUser).thenReturn(testUser);

            // when
            NotificationSubscription result = notificationService.subscribe(testRequest);

            // then
            assertNotNull(result);
            assertEquals(testUserId, result.getUserId());
            assertEquals(testRequest.getFcmToken(), result.getFcmToken());
            assertEquals(testRequest.getRadiusMeters(), result.getRadiusMeters());
            assertEquals(testRequest.getLatitude(), result.getLatitude());
            assertEquals(testRequest.getLongitude(), result.getLongitude());
            assertTrue(result.getIsActive());
            assertNotNull(result.getCreatedAt());

            verify(subscriptionRepository).save(any(NotificationSubscription.class));
        }
    }

    @Test
    void subscribe_shouldUpdateExistingSubscription() {
        // given
        NotificationSubscription existing = NotificationSubscription.builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .fcmToken(testRequest.getFcmToken())
                .build();

        when(subscriptionRepository.findByUserIdAndFcmToken(testUserId, testRequest.getFcmToken()))
                .thenReturn(Optional.of(existing));
        when(subscriptionRepository.save(any(NotificationSubscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<AuthContext> authContextMock = mockStatic(AuthContext.class)) {
            authContextMock.when(AuthContext::getCurrentUser).thenReturn(testUser);

            // when
            NotificationSubscription result = notificationService.subscribe(testRequest);

            // then
            verify(subscriptionRepository).delete(existing);
            verify(subscriptionRepository).save(any(NotificationSubscription.class));
            assertNotNull(result);
        }
    }

    @Test
    void subscribe_shouldThrowIfUserNotAuthenticated() {
        // given
        try (MockedStatic<AuthContext> authContextMock = mockStatic(AuthContext.class)) {
            authContextMock.when(AuthContext::getCurrentUser).thenReturn(null);

            // when & then
            assertThrows(IllegalStateException.class, () -> notificationService.subscribe(testRequest));
            verify(subscriptionRepository, never()).save(any());
        }
    }

    @Test
    void unsubscribe_shouldDeleteSubscription() {
        // given
        try (MockedStatic<AuthContext> authContextMock = mockStatic(AuthContext.class)) {
            authContextMock.when(AuthContext::getCurrentUser).thenReturn(testUser);

            // when
            notificationService.unsubscribe("test-token");

            // then
            verify(subscriptionRepository).deleteByUserIdAndFcmToken(testUserId, "test-token");
        }
    }

    @Test
    void getUserSubscriptions_shouldReturnUserSubscriptions() {
        // given
        List<NotificationSubscription> subscriptions = List.of(
                NotificationSubscription.builder().id(UUID.randomUUID()).build(),
                NotificationSubscription.builder().id(UUID.randomUUID()).build()
        );

        when(subscriptionRepository.findByUserId(testUserId)).thenReturn(subscriptions);

        try (MockedStatic<AuthContext> authContextMock = mockStatic(AuthContext.class)) {
            authContextMock.when(AuthContext::getCurrentUser).thenReturn(testUser);

            // when
            List<NotificationSubscription> result = notificationService.getUserSubscriptions();

            // then
            assertEquals(2, result.size());
            verify(subscriptionRepository).findByUserId(testUserId);
        }
    }

    @Test
    void sendEventCreatedNotification_shouldFindSubscriptionsInRadius() {
        // given
        List<NotificationSubscription> subscriptions = List.of(
                NotificationSubscription.builder()
                        .fcmToken("token1")
                        .isActive(true)
                        .build(),
                NotificationSubscription.builder()
                        .fcmToken("token2")
                        .isActive(true)
                        .build()
        );

        when(subscriptionRepository.findActiveSubscriptionsInRadius(50.0, 20.0))
                .thenReturn(subscriptions);
        when(fcmService.sendNotificationToTokens(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(true));

        // when
        notificationService.sendEventCreatedNotification(50.0, 20.0, "Test Event");

        // then
        verify(fcmService).sendNotificationToTokens(
                List.of("token1", "token2"),
                "New Deal Nearby!",
                "Check out: Test Event"
        );
    }
}
