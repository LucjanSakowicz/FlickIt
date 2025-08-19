package com.flickit.claim.controller;

import com.flickit.claim.dto.ClaimEventRequest;
import com.flickit.claim.dto.RateEventRequest;
import com.flickit.claim.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Claims", description = "Endpoints related to event claiming and rating")
@RestController
@RequestMapping("/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @Operation(summary = "Claim an event")
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> claimEvent(@RequestBody ClaimEventRequest request) {
        boolean success = claimService.claimEvent(request);
        return success ? ResponseEntity.ok().build() : ResponseEntity.status(409).build();
    }

    @Operation(summary = "Rate a claimed event")
    @PostMapping("/rate")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> rateEvent(@Valid @RequestBody RateEventRequest request) {
        boolean success = claimService.rateEvent(request);
        return success ? ResponseEntity.ok().build() : ResponseEntity.status(400).build();
    }
}

