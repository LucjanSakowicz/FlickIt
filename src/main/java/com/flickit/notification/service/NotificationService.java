package com.flickit.notification.service;

import com.flickit.auth.model.CurrentUser;
import com.flickit.auth.service.AuthContext;
import com.flickit.notification.dto.NotificationDto;
import com.flickit.notification.dto.SubscribeRequest;
import com.flickit.notification.model.NotificationSubscription;
import com.flickit.notification.repository.NotificationSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationSubscriptionRepository subscriptionRepository;
    private final FcmService fcmService;

    @Transactional
    public NotificationSubscription subscribe(SubscribeRequest request) {
        CurrentUser currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated");
        }

        // Check if subscription already exists
        subscriptionRepository.findByUserIdAndFcmToken(currentUser.getId(), request.getFcmToken())
                .ifPresent(existing -> {
                    log.info("Updating existing subscription for user {}", currentUser.getId());
                    subscriptionRepository.delete(existing);
                });

        // Create new subscription
        NotificationSubscription subscription = NotificationSubscription.builder()
                .userId(currentUser.getId())
                .fcmToken(request.getFcmToken())
                .radiusMeters(request.getRadiusMeters())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isActive(true)
                .createdAt(Instant.now())
                .build();

        NotificationSubscription saved = subscriptionRepository.save(subscription);
        log.info("User {} subscribed to notifications with radius {}m at ({}, {})", 
            currentUser.getId(), request.getRadiusMeters(), request.getLatitude(), request.getLongitude());

        return saved;
    }

    @Transactional
    public void unsubscribe(String fcmToken) {
        CurrentUser currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated");
        }

        subscriptionRepository.deleteByUserIdAndFcmToken(currentUser.getId(), fcmToken);
        log.info("User {} unsubscribed from notifications with token {}", currentUser.getId(), fcmToken);
    }

    @Transactional(readOnly = true)
    public List<NotificationSubscription> getUserSubscriptions() {
        CurrentUser currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated");
        }

        return subscriptionRepository.findByUserId(currentUser.getId());
    }

    @Transactional
    public void sendEventCreatedNotification(double lat, double lon, String eventTitle) {
        // Find all active subscriptions in radius
        List<NotificationSubscription> subscriptions = subscriptionRepository
                .findActiveSubscriptionsInRadius(lat, lon);

        if (subscriptions.isEmpty()) {
            log.info("No active subscriptions found for event at ({}, {})", lat, lon);
            return;
        }

        // Extract FCM tokens
        List<String> fcmTokens = subscriptions.stream()
                .map(NotificationSubscription::getFcmToken)
                .toList();

        // Send notification
        String title = "New Deal Nearby!";
        String body = String.format("Check out: %s", eventTitle);

        fcmService.sendNotificationToTokens(fcmTokens, title, body)
                .thenAccept(success -> {
                    if (success) {
                        log.info("Event notification sent to {} subscribers", fcmTokens.size());
                        // Update lastNotificationSent for all subscriptions
                        subscriptions.forEach(sub -> {
                            sub.setLastNotificationSent(Instant.now());
                            subscriptionRepository.save(sub);
                        });
                    } else {
                        log.warn("Failed to send event notification to {} subscribers", fcmTokens.size());
                    }
                })
                .exceptionally(throwable -> {
                    log.error("Error sending event notification", throwable);
                    return null;
                });
    }

    @Transactional
    public void sendRatingNotification(UUID userId, String eventTitle, int rating) {
        // Find user's active subscriptions
        List<NotificationSubscription> subscriptions = subscriptionRepository.findByUserId(userId);
        
        if (subscriptions.isEmpty()) {
            log.info("No active subscriptions found for user {}", userId);
            return;
        }

        // Send rating notification
        String title = "Rating Received!";
        String body = String.format("Your event '%s' received a %d-star rating", eventTitle, rating);

        subscriptions.stream()
                .filter(NotificationSubscription::getIsActive)
                .forEach(subscription -> {
                    NotificationDto notification = NotificationDto.builder()
                            .userId(userId)
                            .fcmToken(subscription.getFcmToken())
                            .title(title)
                            .body(body)
                            .type(NotificationDto.NotificationType.RATING_RECEIVED)
                            .status(NotificationDto.NotificationStatus.PENDING)
                            .createdAt(Instant.now())
                            .build();

                    fcmService.sendNotification(notification)
                            .thenAccept(success -> {
                                if (success) {
                                    log.info("Rating notification sent to user {} for event '{}'", userId, eventTitle);
                                    subscription.setLastNotificationSent(Instant.now());
                                    subscriptionRepository.save(subscription);
                                } else {
                                    log.warn("Failed to send rating notification to user {} for event '{}'", userId, eventTitle);
                                }
                            })
                            .exceptionally(throwable -> {
                                log.error("Error sending rating notification", throwable);
                                return null;
                            });
                });
    }
}
