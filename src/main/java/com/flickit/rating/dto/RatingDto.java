package com.flickit.rating.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class RatingDto {
    UUID id;
    UUID eventId;
    UUID userId;
    int rating;
    String comment;
    Instant ratedAt;
}
