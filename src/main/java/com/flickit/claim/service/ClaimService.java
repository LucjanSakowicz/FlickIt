package com.flickit.claim.service;

import com.flickit.claim.dto.ClaimEventRequest;
import com.flickit.claim.dto.RateEventRequest;
import com.flickit.claim.model.ClaimEntity;
import com.flickit.claim.model.ClaimId;
import com.flickit.claim.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;

    public boolean claimEvent(ClaimEventRequest request) {
        return claimEvent(request.getEventId(), request.getUserId());
    }

    public boolean claimEvent(UUID eventId, UUID userId) {
        ClaimId id = new ClaimId(eventId, userId);
        if (claimRepository.existsById(id)) return false;

        ClaimEntity entity = ClaimEntity.builder()
                .id(id)
                .build();

        claimRepository.save(entity);
        return true;
    }

    public boolean rateEvent(RateEventRequest request) {
        ClaimId id = new ClaimId(request.getEventId(), request.getUserId());
        ClaimEntity entity = claimRepository.findById(id).orElse(null);

        if (entity == null || entity.getRating() != null) return false;

        entity.setRating(request.getRating());
        entity.setComment(request.getComment());
        entity.setRatedAt(request.getRatedAt() != null ? request.getRatedAt() : Instant.now());

        claimRepository.save(entity);
        return true;
    }
}
