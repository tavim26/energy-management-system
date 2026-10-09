package com.energymanagement.customersupportservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Calls the Google Gemini "generateContent" REST endpoint
@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    private final RestClient restClient;
    private final String apiKey;
    private final String apiUrl;

    public GeminiClient(@Value("${gemini.api.key}") String apiKey, @Value("${gemini.api.url}") String apiUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(20));

        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    // Empty when the API cannot be reached or returns no text
    public Optional<String> generate(String systemInstruction, String userMessage) {
        GeminiRequest request = new GeminiRequest(
                new Content(null, List.of(new Part(systemInstruction))),
                List.of(new Content("user", List.of(new Part(userMessage))))
        );

        try {
            GeminiResponse response = restClient.post()
                    .uri(apiUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    // Sent as a header, not as a URL parameter, so the key never shows up in logged URLs
                    .header("x-goog-api-key", apiKey)
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class);

            return Optional.ofNullable(response).flatMap(GeminiResponse::text);

        } catch (RestClientException e) {
            log.error("Gemini request failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public record GeminiRequest(
            @JsonProperty("system_instruction") Content systemInstruction,
            List<Content> contents
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Content(String role, List<Part> parts) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Part(String text) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Candidate(Content content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeminiResponse(List<Candidate> candidates) {

        // The answer is the text of the first candidate, which can be split into several parts
        Optional<String> text() {
            if (candidates == null || candidates.isEmpty()) {
                return Optional.empty();
            }

            Content content = candidates.get(0).content();

            if (content == null || content.parts() == null) {
                return Optional.empty();
            }

            String text = content.parts().stream()
                    .map(Part::text)
                    .filter(part -> part != null && !part.isBlank())
                    .collect(Collectors.joining());

            return text.isBlank() ? Optional.empty() : Optional.of(text.trim());
        }
    }
}