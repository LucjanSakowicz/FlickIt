package com.flickit.claim.service;

import com.flickit.claim.model.ClaimEntity;
import com.flickit.claim.model.ClaimId;
import com.flickit.claim.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;

    public boolean claimEvent(UUID eventId, UUID userId) {
        ClaimId id = new ClaimId(eventId, userId);
        if (claimRepository.existsById(id)) return false;

        ClaimEntity entity = ClaimEntity.builder()
                .id(id)
                .build();

        claimRepository.save(entity);
        return true;
    }


}
