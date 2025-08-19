package com.flickit.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisionLabelDto {
    
    private String label;
    private Double confidence;
    
    public VisionLabelDto(String label, double confidence) {
        this.label = label;
        this.confidence = confidence;
    }
}
