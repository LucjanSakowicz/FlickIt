package com.flickit.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GenerateContentRequest {
    
    @NotBlank(message = "Image URL is required")
    private String imageUrl;
    
    @Size(max = 500, message = "Prompt cannot exceed 500 characters")
    private String additionalPrompt;
    
    private String businessType; // e.g., "restaurant", "shop", "service"
    
    private String preferredLanguage = "pl"; // Default to Polish for FlickIt
}
