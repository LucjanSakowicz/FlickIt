package com.flickit.claim.service;

import com.flickit.claim.dto.RateEventRequest;
import com.flickit.claim.model.ClaimEntity;
import com.flickit.claim.model.ClaimId;
import com.flickit.claim.repository.ClaimRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClaimServiceTest {

    @Test
    void rateEvent_shouldUpdateExistingClaim() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ClaimId id = new ClaimId(eventId, userId);

        ClaimEntity mockClaim = new ClaimEntity();
        mockClaim.setId(id);

        ClaimRepository repo = mock(ClaimRepository.class);
        when(repo.findById(id)).thenReturn(Optional.of(mockClaim));

        ClaimService service = new ClaimService(repo);

        RateEventRequest request = RateEventRequest.builder()
                .eventId(eventId)
                .userId(userId)
                .rating(4)
                .comment("Nice deal!")
                .ratedAt(Instant.now())
                .build();

        boolean result = service.rateEvent(request);

        assertTrue(result);
        assertEquals(4, mockClaim.getRating());
        assertEquals("Nice deal!", mockClaim.getComment());
        verify(repo, times(1)).save(mockClaim);
    }

    @Test
    void rateEvent_shouldFailIfNoClaim() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ClaimId id = new ClaimId(eventId, userId);

        ClaimRepository repo = mock(ClaimRepository.class);
        when(repo.findById(id)).thenReturn(Optional.empty());

        ClaimService service = new ClaimService(repo);

        RateEventRequest request = RateEventRequest.builder()
                .eventId(eventId)
                .userId(userId)
                .rating(5)
                .build();

        assertFalse(service.rateEvent(request));
    }
}
