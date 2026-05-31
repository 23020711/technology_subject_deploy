package com.pricehawl.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiLlmClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.base-url:}")
    private String baseUrl;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.model:gpt-3.5-turbo}")
    private String model;

    public String generateAnswer(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank() || baseUrl.isBlank()) {
            System.err.println("AI features are disabled: Missing API Key or Base URL.");
            return null;
        }

        try {
            String url = baseUrl + "/v1beta/models/" + model + ":generateContent?key=" + apiKey;

            Map<String, Object> body = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "role", "user",
                                    "parts", List.of(
                                            Map.of(
                                                    "text",
                                                    systemPrompt + "\n\n" + userPrompt
                                            )
                                    )
                            )
                    ),
                    "generationConfig", Map.of(
                            "temperature", 0.2,
                            "maxOutputTokens", 800
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    JsonNode.class
            );

            if (response.getBody() == null) {
                return null;
            }

            JsonNode contentNode = response.getBody().at("/candidates/0/content/parts/0/text");
            if (contentNode.isMissingNode()) {
                return null;
            }

            return contentNode.asText();

        } catch (Exception e) {
            System.err.println("AI call failed: " + e.getMessage());
            return null;
        }
    }
}