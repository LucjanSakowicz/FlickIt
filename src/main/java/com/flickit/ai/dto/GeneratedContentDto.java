package com.flickit.ai.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class GeneratedContentDto {
    
    private String title;
    private String description;
    private List<VisionLabelDto> labels;
    private String suggestedCategory;
    private Double confidence;
    private String modelUsed;
    private Instant generatedAt;
    private Integer tokensUsed;
}
