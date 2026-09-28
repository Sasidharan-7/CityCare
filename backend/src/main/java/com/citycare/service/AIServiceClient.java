package com.citycare.service;

import com.citycare.dto.AIAnalysisResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class AIServiceClient {

    private final RestTemplate restTemplate;
    private final String aiServiceUrl;

    public AIServiceClient(@Value("${citycare.ai.service-url:http://localhost:5000}") String aiServiceUrl,
                           RestTemplateBuilder restTemplateBuilder) {
        this.aiServiceUrl = aiServiceUrl;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Calls external AI microservice to analyze image & description.
     * Falls back seamlessly to internal rule engine if microservice is offline.
     */
    public AIAnalysisResponse analyzeComplaint(String category, String description, String imageUrl) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> payload = new HashMap<>();
            payload.put("category", category != null ? category : "");
            payload.put("description", description != null ? description : "");
            payload.put("imageUrl", imageUrl != null ? imageUrl : "");

            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<AIAnalysisResponse> response = restTemplate.postForEntity(
                    aiServiceUrl + "/classify",
                    request,
                    AIAnalysisResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            // Log fallback without failing user's complaint submission
        }

        // Graceful Fallback Engine (Phase 7 Rule-Based Priority)
        return calculateFallbackAnalysis(category, description);
    }

    public AIAnalysisResponse calculateFallbackAnalysis(String category, String description) {
        AIAnalysisResponse fallback = new AIAnalysisResponse();
        String cat = (category != null && !category.isBlank()) ? category.toUpperCase() : "OTHER";
        String desc = (description != null) ? description.toLowerCase() : "";

        fallback.setCategory(cat);
        fallback.setConfidence(0.85);
        fallback.setEngine("java_fallback_rule_engine");
        fallback.setIsTrainedModel(false);

        // Priority calculation rules
        if (cat.equals("OPEN_DRAIN") || desc.contains("manhole") || desc.contains("school") || desc.contains("hospital") || desc.contains("danger")) {
            fallback.setPriority("CRITICAL");
        } else if (cat.equals("POTHOLE") || cat.equals("ROAD_DAMAGE") || cat.equals("WATER_LEAKAGE") || desc.contains("highway") || desc.contains("burst")) {
            fallback.setPriority("HIGH");
        } else if (cat.equals("GARBAGE") || cat.equals("BROKEN_STREETLIGHT")) {
            fallback.setPriority("MEDIUM");
        } else {
            fallback.setPriority("LOW");
        }

        return fallback;
    }
}
