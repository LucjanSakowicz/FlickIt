package com.flickit.notification.repository;

import com.flickit.notification.model.NotificationSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationSubscriptionRepository extends JpaRepository<NotificationSubscription, UUID> {

    Optional<NotificationSubscription> findByUserIdAndFcmToken(UUID userId, String fcmToken);

    List<NotificationSubscription> findByUserId(UUID userId);

    // For MVP - using simple Haversine formula (works with H2 and PostgreSQL)
    @Query("SELECT ns FROM NotificationSubscription ns " +
           "WHERE ns.isActive = true " +
           "AND (6371000 * acos(cos(radians(:lat)) * cos(radians(ns.latitude)) * " +
           "cos(radians(ns.longitude) - radians(:lon)) + sin(radians(:lat)) * sin(radians(ns.latitude)))) <= ns.radiusMeters")
    List<NotificationSubscription> findActiveSubscriptionsInRadius(@Param("lat") double lat, @Param("lon") double lon);

    void deleteByUserIdAndFcmToken(UUID userId, String fcmToken);
}
