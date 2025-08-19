package com.flickit.event.dto;

import com.flickit.event.model.EventEntity;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class EventDto {
    private UUID id;
    private String titleAi;
    private String titleVendor;
    private String descriptionAi;
    private String descriptionVendor;
    private double lat;
    private double lon;
    private Double alt;
    private Integer floor;
    private EventEntity.Category category;
    private Instant expiresAt;
    private EventEntity.Status status;
    private UUID vendorId;
    
    // For display - computed fields
    private String title; // titleVendor || titleAi
    private String description; // descriptionVendor || descriptionAi
    
    // Additional fields
    private List<String> imageUrls;
    private String aiImageUrl;
    private String discount;
    private String style;
}