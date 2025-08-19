package com.flickit.rating.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flickit.auth.model.CurrentUser;
import com.flickit.auth.service.AuthContext;
import com.flickit.event.repository.EventRepository;
import com.flickit.notification.service.NotificationService;
import com.flickit.rating.dto.CreateRatingRequest;
import com.flickit.rating.dto.RatingDto;
import com.flickit.rating.model.RatingEntity;
import com.flickit.rating.repository.RatingRepository;
import com.flickit.user.model.UserEntity;
import com.flickit.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final NotificationService notificationService;

    @Transactional
    public RatingDto rateEvent(CreateRatingRequest request) {
        CurrentUser current = AuthContext.getCurrentUser();
        if (current == null) {
            throw new IllegalStateException("Unauthenticated access - this should not happen.");
        }
        if (ratingRepository.existsByEventIdAndUserId(request.getEventId(), current.getId())) {
            throw new IllegalArgumentException("You have already rated this event.");
        }

        RatingEntity entity = RatingEntity.builder()
                .eventId(request.getEventId())
                .userId(current.getId())
                .rating(request.getRating())
                .comment(request.getComment())
                .ratedAt(Instant.now())
                .build();

        RatingEntity saved = ratingRepository.save(entity);

        updateVendorRating(request.getEventId(), request.getRating());

        // Send notification to vendor about new rating
        UUID vendorId = getVendorIdFromEvent(request.getEventId());
        String eventTitle = getEventTitle(request.getEventId());
        notificationService.sendRatingNotification(vendorId, eventTitle, request.getRating());

        return objectMapper.convertValue(saved, RatingDto.class);
    }

    private void updateVendorRating(UUID eventId, int newRating) {
        UUID vendorId = getVendorIdFromEvent(eventId);
        UserEntity vendor = userRepository.findById(vendorId)
                .orElseThrow(() -> new IllegalStateException("Vendor not found"));

        double currentSum = (vendor.getRating() == null ? 0.0 : vendor.getRating()) *
                (vendor.getRatingCount() == null ? 0 : vendor.getRatingCount());

        int newCount = (vendor.getRatingCount() == null ? 0 : vendor.getRatingCount()) + 1;
        double newAverage = (currentSum + newRating) / newCount;

        vendor.setRating(Math.round(newAverage * 100.0) / 100.0); // zaokrąglenie
        vendor.setRatingCount(newCount);

        userRepository.save(vendor);
    }

    private UUID getVendorIdFromEvent(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"))
                .getVendorId();
    }

    private String getEventTitle(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"))
                .getTitleVendor() != null ? 
                    eventRepository.findById(eventId).get().getTitleVendor() : 
                    eventRepository.findById(eventId).get().getTitleAi();
    }
}
