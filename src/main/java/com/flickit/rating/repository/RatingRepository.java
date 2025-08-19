package com.flickit.rating.repository;

import com.flickit.rating.model.RatingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RatingRepository extends JpaRepository<RatingEntity, UUID> {
    boolean existsByEventIdAndUserId(UUID eventId, UUID userId);
    Optional<RatingEntity> findByEventIdAndUserId(UUID eventId, UUID userId);
}
