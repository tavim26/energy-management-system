package com.energymanagement.customersupportservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiSupportService
{
    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateResponse(String userMessage)
    {
        if (apiKey == null || apiKey.isBlank())
        {
            return "AI Assistent is not configured.";
        }
        // Construieste request body
        Map<String, Object> requestBody = new HashMap<>();

        Map<String, Object> part = new HashMap<>();
        part.put("text", userMessage);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));

        requestBody.put("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Sent as a header, not as a URL parameter, so the key never shows up in logged URLs
        headers.set("x-goog-api-key", apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            String urlWithKey = apiUrl + "?key=" + apiKey;

            ResponseEntity<Map> response = restTemplate.exchange(
                    urlWithKey,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();

            if (responseBody == null || !responseBody.containsKey("candidates"))
            {
                throw new RuntimeException("Invalid Gemini API response");
            }

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            Map<String, Object> candidateContent = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, String>> parts = (List<Map<String, String>>) candidateContent.get("parts");

            return parts.get(0).get("text");

        } catch (Exception e) {

            System.err.println("AI service error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("AI service temporarily unavailable");
        }
    }
}