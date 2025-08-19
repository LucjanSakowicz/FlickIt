package com.flickit.ai.service;

import com.flickit.ai.dto.GenerateContentRequest;
import com.flickit.ai.dto.GeneratedContentDto;
import com.flickit.ai.dto.VisionLabelDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AIServiceTest {

    @InjectMocks
    private AIService aiService;

    private GenerateContentRequest testRequest;

    @BeforeEach
    void setUp() {
        testRequest = new GenerateContentRequest();
        testRequest.setImageUrl("https://example.com/food-image.jpg");
        testRequest.setBusinessType("restaurant");
        testRequest.setAdditionalPrompt("Specjalna promocja");
        testRequest.setPreferredLanguage("pl");
    }

    @Test
    void generateContent_shouldReturnValidContent() {
        // when
        GeneratedContentDto result = aiService.generateContent(testRequest);

        // then
        assertNotNull(result);
        assertNotNull(result.getTitle());
        assertNotNull(result.getDescription());
        assertNotNull(result.getLabels());
        assertNotNull(result.getSuggestedCategory());
        assertNotNull(result.getGeneratedAt());
        assertNotNull(result.getModelUsed());
        
        assertFalse(result.getTitle().isEmpty());
        assertFalse(result.getDescription().isEmpty());
        assertFalse(result.getLabels().isEmpty());
        
        assertTrue(result.getConfidence() >= 0.85);
        assertTrue(result.getConfidence() <= 1.0);
        assertTrue(result.getTokensUsed() >= 150);
        assertTrue(result.getTokensUsed() <= 250);
        
        assertEquals("gpt-4-vision-preview-mock", result.getModelUsed());
    }

    @Test
    void generateContent_shouldDetectFoodLabels() {
        // given
        testRequest.setImageUrl("https://example.com/food-restaurant-pizza.jpg");
        testRequest.setBusinessType("restaurant");

        // when
        GeneratedContentDto result = aiService.generateContent(testRequest);

        // then
        assertTrue(result.getLabels().stream()
            .anyMatch(label -> label.getLabel().contains("food")));
        assertTrue(result.getSuggestedCategory().equals("FOOD"));
    }

    @Test
    void generateContent_shouldDetectShopLabels() {
        // given
        testRequest.setImageUrl("https://example.com/shop-store-retail.jpg");
        testRequest.setBusinessType("retail");

        // when
        GeneratedContentDto result = aiService.generateContent(testRequest);

        // then
        assertTrue(result.getLabels().stream()
            .anyMatch(label -> label.getLabel().contains("shop") || label.getLabel().contains("retail")));
        assertTrue(result.getSuggestedCategory().equals("OTHER"));
    }

    @Test
    void generateContent_shouldDetectServiceLabels() {
        // given
        testRequest.setImageUrl("https://example.com/service-salon-spa.jpg");
        testRequest.setBusinessType("service");

        // when
        GeneratedContentDto result = aiService.generateContent(testRequest);

        // then
        assertTrue(result.getLabels().stream()
            .anyMatch(label -> label.getLabel().contains("service")));
        assertTrue(result.getSuggestedCategory().equals("SERVICE"));
    }

    @Test
    void generateContent_shouldIncludeAdditionalPrompt() {
        // given
        testRequest.setAdditionalPrompt("Promotion special");

        // when
        GeneratedContentDto result = aiService.generateContent(testRequest);

        // then
        assertTrue(result.getDescription().contains("Promotion special"));
    }

    @Test
    void generateContent_shouldHandleNullBusinessType() {
        // given
        testRequest.setBusinessType(null);

        // when
        GeneratedContentDto result = aiService.generateContent(testRequest);

        // then
        assertNotNull(result);
        assertNotNull(result.getTitle());
        assertNotNull(result.getDescription());
        assertFalse(result.getTitle().isEmpty());
        assertFalse(result.getDescription().isEmpty());
    }

    @Test
    void generateContent_shouldHandleNullAdditionalPrompt() {
        // given
        testRequest.setAdditionalPrompt(null);

        // when
        GeneratedContentDto result = aiService.generateContent(testRequest);

        // then
        assertNotNull(result);
        assertNotNull(result.getDescription());
        assertFalse(result.getDescription().isEmpty());
    }

    @Test
    void generateContent_shouldGeneratePolishContent() {
        // when
        GeneratedContentDto result = aiService.generateContent(testRequest);

        // then
        // Check if content is in Polish (contains Polish characters or words)
        String content = result.getTitle() + " " + result.getDescription();
        assertTrue(content.contains("ą") || content.contains("ć") || content.contains("ę") || 
                  content.contains("ł") || content.contains("ń") || content.contains("ó") || 
                  content.contains("ś") || content.contains("ź") || content.contains("ż") ||
                  content.contains("Oferta") || content.contains("Specjalna") || content.contains("Promocja"));
    }

    @Test
    void generateContent_shouldHaveReasonableConfidenceLevels() {
        // when
        GeneratedContentDto result = aiService.generateContent(testRequest);

        // then
        for (VisionLabelDto label : result.getLabels()) {
            assertTrue(label.getConfidence() >= 0.0);
            assertTrue(label.getConfidence() <= 1.0);
            assertNotNull(label.getLabel());
            assertFalse(label.getLabel().isEmpty());
        }
    }
}
