package com.flickit.notification.controller;

import com.flickit.notification.dto.SubscribeRequest;
import com.flickit.notification.model.NotificationSubscription;
import com.flickit.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification subscription and management endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/subscribe")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Subscribe to notifications", description = "Subscribe to push notifications for events in specified radius")
    public ResponseEntity<NotificationSubscription> subscribe(@RequestBody @Valid SubscribeRequest request) {
        NotificationSubscription subscription = notificationService.subscribe(request);
        return ResponseEntity.ok(subscription);
    }

    @DeleteMapping("/unsubscribe")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Unsubscribe from notifications", description = "Unsubscribe from push notifications using FCM token")
    public ResponseEntity<Void> unsubscribe(@RequestParam String fcmToken) {
        notificationService.unsubscribe(fcmToken);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/subscriptions")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get user subscriptions", description = "Get all active notification subscriptions for current user")
    public ResponseEntity<List<NotificationSubscription>> getUserSubscriptions() {
        List<NotificationSubscription> subscriptions = notificationService.getUserSubscriptions();
        return ResponseEntity.ok(subscriptions);
    }

    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Send test notification", description = "Admin endpoint to send test notification to current user")
    public ResponseEntity<String> sendTestNotification() {
        // For MVP - just return success
        // Later: implement actual test notification
        return ResponseEntity.ok("Test notification would be sent (MVP stub)");
    }
}
