package com.flickit.ai.service;

import com.flickit.ai.dto.GenerateContentRequest;
import com.flickit.ai.dto.GeneratedContentDto;
import com.flickit.ai.dto.VisionLabelDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class AIService {

    private final Random random = new Random();

    // For MVP - mock AI content generation
    // Later: integrate with OpenAI Vision + GPT API

    public GeneratedContentDto generateContent(GenerateContentRequest request) {
        log.info("Generating AI content for image: {}", request.getImageUrl());
        
        // Simulate processing delay
        try {
            Thread.sleep(500 + random.nextInt(1000)); // 0.5-1.5s delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Mock vision analysis based on image URL patterns
        List<VisionLabelDto> labels = generateMockLabels(request.getImageUrl());
        
        // Generate mock title and description
        String title = generateMockTitle(labels, request.getBusinessType());
        String description = generateMockDescription(title, labels, request.getAdditionalPrompt());
        String suggestedCategory = suggestCategory(labels);
        
        GeneratedContentDto result = GeneratedContentDto.builder()
                .title(title)
                .description(description)
                .labels(labels)
                .suggestedCategory(suggestedCategory)
                .confidence(0.85 + random.nextDouble() * 0.1) // 85-95% confidence
                .modelUsed("gpt-4-vision-preview-mock")
                .generatedAt(Instant.now())
                .tokensUsed(150 + random.nextInt(100)) // 150-250 tokens
                .build();

        log.info("Generated AI content: title='{}', category='{}', confidence={}", 
            result.getTitle(), result.getSuggestedCategory(), result.getConfidence());
            
        return result;
    }

    private List<VisionLabelDto> generateMockLabels(String imageUrl) {
        // Mock labels based on common business types
        String url = imageUrl.toLowerCase();
        
        if (url.contains("food") || url.contains("restaurant") || url.contains("pizza") || url.contains("burger")) {
            return Arrays.asList(
                new VisionLabelDto("food", 0.95),
                new VisionLabelDto("restaurant", 0.88),
                new VisionLabelDto("dining", 0.82),
                new VisionLabelDto("meal", 0.76)
            );
        } else if (url.contains("shop") || url.contains("store") || url.contains("retail")) {
            return Arrays.asList(
                new VisionLabelDto("shop", 0.92),
                new VisionLabelDto("retail", 0.85),
                new VisionLabelDto("products", 0.79),
                new VisionLabelDto("store", 0.87)
            );
        } else if (url.contains("service") || url.contains("salon") || url.contains("spa")) {
            return Arrays.asList(
                new VisionLabelDto("service", 0.89),
                new VisionLabelDto("business", 0.83),
                new VisionLabelDto("professional", 0.77)
            );
        } else {
            // Generic labels
            return Arrays.asList(
                new VisionLabelDto("business", 0.80),
                new VisionLabelDto("establishment", 0.75),
                new VisionLabelDto("commercial", 0.70)
            );
        }
    }

    private String generateMockTitle(List<VisionLabelDto> labels, String businessType) {
        String[] foodTitles = {
            "Pyszne Jedzenie w Świetnej Cenie! 🍕",
            "Nie Do Przegapienia - Lunch Specjal! 🥗",
            "Flash Sale: Ulubione Dania -30%! 🍔",
            "Gorące Nowości w Menu! 🌶️"
        };
        
        String[] shopTitles = {
            "Mega Wyprzedaż - Tylko Dziś! 🛍️",
            "Nowe Produkty w Sklepie! ✨",
            "Flash Sale: Wszystko -50%! 💫",
            "Limitowana Oferta Specjalna! 🎯"
        };
        
        String[] serviceTitles = {
            "Ekskluzywna Usługa w Promocji! 💆",
            "Specjalna Oferta na Usługi! ⭐",
            "Flash Offer: Premium Service! 👑",
            "Nie Przegap - Tylko Dzisiaj! ⚡"
        };

        if (businessType != null) {
            businessType = businessType.toLowerCase();
            if (businessType.contains("food") || businessType.contains("restaurant")) {
                return foodTitles[random.nextInt(foodTitles.length)];
            } else if (businessType.contains("shop") || businessType.contains("retail")) {
                return shopTitles[random.nextInt(shopTitles.length)];
            } else if (businessType.contains("service")) {
                return serviceTitles[random.nextInt(serviceTitles.length)];
            }
        }

        // Fallback based on labels
        if (labels.stream().anyMatch(l -> l.getLabel().contains("food") || l.getLabel().contains("restaurant"))) {
            return foodTitles[random.nextInt(foodTitles.length)];
        } else if (labels.stream().anyMatch(l -> l.getLabel().contains("shop") || l.getLabel().contains("retail"))) {
            return shopTitles[random.nextInt(shopTitles.length)];
        } else if (labels.stream().anyMatch(l -> l.getLabel().contains("service"))) {
            return serviceTitles[random.nextInt(serviceTitles.length)];
        }

        return "Niesamowita Oferta Specjalna! 🎉";
    }

    private String generateMockDescription(String title, List<VisionLabelDto> labels, String additionalPrompt) {
        String baseDescription = "Sprawdź naszą wyjątkową ofertę! Idealna okazja na odkrycie czegoś nowego. ";
        
        if (labels.stream().anyMatch(l -> l.getLabel().contains("food"))) {
            baseDescription = "Skosztuj najlepszych smaków w okolicy! Świeże składniki, doskonała jakość. ";
        } else if (labels.stream().anyMatch(l -> l.getLabel().contains("shop"))) {
            baseDescription = "Odkryj niesamowite produkty w naszym sklepie! Najwyższa jakość w świetnych cenach. ";
        } else if (labels.stream().anyMatch(l -> l.getLabel().contains("service"))) {
            baseDescription = "Skorzystaj z naszych profesjonalnych usług! Doświadczony zespół czeka na Ciebie. ";
        }

        String timeLimit = "Oferta ważna tylko przez ograniczony czas - nie zwlekaj!";
        
        if (additionalPrompt != null && !additionalPrompt.trim().isEmpty()) {
            baseDescription += additionalPrompt + " ";
        }
        
        return baseDescription + timeLimit;
    }

    private String suggestCategory(List<VisionLabelDto> labels) {
        if (labels.stream().anyMatch(l -> l.getLabel().contains("food") || l.getLabel().contains("restaurant"))) {
            return "FOOD";
        } else if (labels.stream().anyMatch(l -> l.getLabel().contains("service"))) {
            return "SERVICE";
        } else {
            return "OTHER";
        }
    }
}
