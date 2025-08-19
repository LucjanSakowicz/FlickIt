package com.flickit.claim.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimEventRequest {
    private UUID eventId;
    private UUID userId;
}
