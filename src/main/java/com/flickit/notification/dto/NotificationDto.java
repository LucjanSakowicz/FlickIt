package com.flickit.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class NotificationDto {

    private UUID id;
    private UUID userId;
    private String title;
    private String body;
    private String fcmToken;
    private NotificationType type;
    private NotificationStatus status;
    private Instant createdAt;
    private Instant sentAt;
    private String errorMessage;

    public enum NotificationType {
        EVENT_CREATED,
        EVENT_EXPIRING,
        EVENT_CLAIMED,
        RATING_RECEIVED
    }

    public enum NotificationStatus {
        PENDING,
        SENT,
        FAILED,
        CANCELLED
    }
}
