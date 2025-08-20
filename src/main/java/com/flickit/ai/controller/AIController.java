package com.flickit.ai.controller;

import com.flickit.ai.dto.GenerateContentRequest;
import com.flickit.ai.dto.GeneratedContentDto;
import com.flickit.ai.service.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@Tag(name = "AI Service", description = "AI-powered content generation and analysis endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/generate-content")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @Operation(
        summary = "Generate AI content from image",
        description = "Analyzes an image and generates title, description, and category suggestions using AI vision analysis. " +
                    "Available for VENDOR and ADMIN roles only.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Image analysis request with business context",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GenerateContentRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Restaurant Example",
                        summary = "Food business analysis",
                        description = "Example request for a restaurant business",
                        value = """
                        {
                            "imageUrl": "https://example.com/restaurant-interior.jpg",
                            "businessType": "restaurant",
                            "additionalPrompt": "Specjalna promocja na lunch",
                            "preferredLanguage": "pl"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Service Business Example",
                        summary = "Service business analysis",
                        description = "Example request for a service business",
                        value = """
                        {
                            "imageUrl": "https://example.com/service-office.jpg",
                            "businessType": "service",
                            "additionalPrompt": "Profesjonalne usługi konsultingowe",
                            "preferredLanguage": "pl"
                        }
                        """
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Content generated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GeneratedContentDto.class),
                examples = {
                    @ExampleObject(
                        name = "Restaurant Response",
                        summary = "Generated content for restaurant",
                        value = """
                        {
                            "title": "Elegancka Restauracja z Promocją Lunch",
                            "description": "Odkryj nasze wyjątkowe menu lunchowe w eleganckim wnętrzu. " +
                                         "Specjalne promocje dostępne codziennie w godzinach 12:00-15:00.",
                            "suggestedCategory": "FOOD",
                            "confidence": 0.92,
                            "modelUsed": "gpt-4-vision-preview",
                            "processingTimeMs": 1250
                        }
                        """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request - missing required fields or invalid image URL",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Missing Image URL",
                        value = """
                        {
                            "timestamp": "2025-08-19T20:00:00Z",
                            "status": 400,
                            "error": "Bad Request",
                            "message": "Image URL is required",
                            "path": "/ai/generate-content"
                        }
                        """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - insufficient role permissions",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Insufficient Role",
                        value = """
                        {
                            "timestamp": "2025-08-19T20:00:00Z",
                            "status": 403,
                            "error": "Forbidden",
                            "message": "Access denied. Required roles: VENDOR, ADMIN",
                            "path": "/ai/generate-content"
                        }
                        """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error - AI service unavailable",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "AI Service Error",
                        value = """
                        {
                            "timestamp": "2025-08-19T20:00:00Z",
                            "status": 500,
                            "error": "Internal Server Error",
                            "message": "AI service temporarily unavailable",
                            "path": "/ai/generate-content"
                        }
                        """
                    )
                }
            )
        )
    })
    public ResponseEntity<GeneratedContentDto> generateContent(
            @Parameter(
                description = "Image analysis request with business context and preferences",
                required = true
            )
            @RequestBody GenerateContentRequest request) {
        
        GeneratedContentDto result = aiService.generateContent(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Test AI content generation",
        description = "Generates test content using predefined parameters for testing purposes. " +
                    "Available for ADMIN role only.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Test content generated successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = GeneratedContentDto.class),
                    examples = {
                        @ExampleObject(
                            name = "Test Response",
                            summary = "Generated test content",
                            value = """
                            {
                                "title": "Test Event Title",
                                "description": "This is a test description generated for testing purposes.",
                                "suggestedCategory": "OTHER",
                                "confidence": 0.95,
                                "modelUsed": "gpt-4-test",
                                "processingTimeMs": 500
                            }
                            """
                        )
                    }
                )
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Access denied - ADMIN role required",
                content = @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "Admin Role Required",
                            value = """
                            {
                                "timestamp": "2025-08-19T20:00:00Z",
                                "status": 403,
                                "error": "Forbidden",
                                "message": "Access denied. Required role: ADMIN",
                                "path": "/ai/test"
                            }
                            """
                        )
                    }
                )
            )
        }
    )
    public ResponseEntity<GeneratedContentDto> testGeneration() {
        GeneratedContentDto result = aiService.generateContent(new GenerateContentRequest());
        return ResponseEntity.ok(result);
    }
}
