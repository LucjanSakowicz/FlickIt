package com.flickit.event.repository;

import com.flickit.event.model.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, UUID> {
    List<EventEntity> findByLatBetweenAndLonBetween(double minLat, double maxLat, double minLon, double maxLon);
}
