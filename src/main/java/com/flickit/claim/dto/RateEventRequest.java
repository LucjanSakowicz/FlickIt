package com.flickit.claim.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateEventRequest {

    @NotNull
    private UUID eventId;

    @NotNull
    private UUID userId;

    @Min(1)
    @Max(5)
    private int rating;

    private String comment;

    private Instant ratedAt;
}
