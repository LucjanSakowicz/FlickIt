package com.flickit.rating.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateRatingRequest {
    @NotNull
    private UUID eventId;

    @Min(1)
    @Max(5)
    private int rating;

    private String comment;
}
