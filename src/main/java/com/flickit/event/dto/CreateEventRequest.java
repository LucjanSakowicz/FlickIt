package com.flickit.event.dto;

import com.flickit.event.model.EventEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class CreateEventRequest {
    
    @NotBlank
    private String titleVendor; // Vendor can override AI title
    
    private String descriptionVendor; // Vendor can override AI description
    
    @NotNull
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double lat;
    
    @NotNull
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double lon;
    
    private Double alt; // Altitude (optional)
    private Integer floor; // Floor number (optional)
    
    @NotNull
    private EventEntity.Category category;
    
    @NotNull
    @Future(message = "Event must expire in the future")
    private Instant expiresAt;
    
    // For MVP - simplified image handling (later will be base64 images)
    private List<String> imageUrls;
    private String aiImageUrl; // Which image to use for AI processing
    
    // Additional fields for specific categories
    private String discount; // e.g., "20% off", "Buy 1 Get 1"
    private String style; // e.g., "casual", "formal", "family-friendly"
} 