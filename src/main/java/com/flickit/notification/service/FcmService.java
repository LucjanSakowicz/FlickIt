package com.flickit.notification.service;

import com.flickit.notification.dto.NotificationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class FcmService {

    // For MVP - mock FCM service
    // Later: integrate with Firebase Admin SDK

    public CompletableFuture<Boolean> sendNotification(NotificationDto notification) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Sending FCM notification to token: {}, title: '{}', body: '{}'", 
                    notification.getFcmToken(), notification.getTitle(), notification.getBody());
                
                // Simulate FCM API call
                Thread.sleep(100); // Simulate network delay
                
                // For MVP - always succeed
                // Later: implement actual FCM API call
                boolean success = Math.random() > 0.1; // 90% success rate for testing
                
                if (success) {
                    log.info("FCM notification sent successfully");
                    return true;
                } else {
                    log.warn("FCM notification failed (simulated)");
                    return false;
                }
            } catch (Exception e) {
                log.error("Error sending FCM notification", e);
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> sendNotificationToTokens(List<String> fcmTokens, String title, String body) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Sending FCM notification to {} tokens: title='{}', body='{}'", 
                    fcmTokens.size(), title, body);
                
                // Simulate batch FCM API call
                Thread.sleep(200); // Simulate network delay
                
                // For MVP - always succeed
                // Later: implement actual FCM batch API call
                boolean success = Math.random() > 0.05; // 95% success rate for testing
                
                if (success) {
                    log.info("FCM batch notification sent successfully to {} tokens", fcmTokens.size());
                    return true;
                } else {
                    log.warn("FCM batch notification failed (simulated)");
                    return false;
                }
            } catch (Exception e) {
                log.error("Error sending FCM batch notification", e);
                return false;
            }
        });
    }

    public void sendEventCreatedNotification(double lat, double lon, String eventTitle) {
        // This will be called when a new event is created
        // For MVP - just log
        // Later: find subscribers in radius and send notifications
        log.info("Event created notification would be sent for event '{}' at ({}, {})", eventTitle, lat, lon);
    }
}
