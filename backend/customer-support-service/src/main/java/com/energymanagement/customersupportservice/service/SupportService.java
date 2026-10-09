package com.energymanagement.customersupportservice.service;

import com.energymanagement.customersupportservice.client.GeminiClient;
import com.energymanagement.customersupportservice.dto.ChatResponse;
import com.energymanagement.customersupportservice.dto.ResponseSource;
import org.springframework.stereotype.Service;

import java.util.Optional;

// Answers a chat message: predefined rules first, then the AI, then a fallback message
@Service
public class SupportService {

    // Keeps the AI on topic and its answers short
    private static final String SYSTEM_INSTRUCTION = """
            You are the support assistant of an energy management web application.
            In this application, administrators manage users and devices, and assign devices to clients.
            Each device sends its consumption every 10 minutes, the values are added up per hour,
            and a client receives a real-time alert when a device exceeds its maximum hourly consumption (kWh).
            Answer in English, in at most three short sentences.
            If the question is not about the application, energy consumption or saving energy,
            politely say that you can only help with those topics.
            """;

    private static final String NOT_UNDERSTOOD = "Sorry, I did not understand your question. "
            + "I can help with your account, your devices, energy consumption, limits and alerts.";

    private static final String AI_UNAVAILABLE = "The assistant is temporarily unavailable. "
            + "Please try again later, or ask about your account, devices, consumption or alerts.";

    private final RuleBasedSupportService ruleBasedService;
    private final GeminiClient geminiClient;

    public SupportService(RuleBasedSupportService ruleBasedService, GeminiClient geminiClient) {
        this.ruleBasedService = ruleBasedService;
        this.geminiClient = geminiClient;
    }

    public ChatResponse answer(String message) {
        Optional<String> ruleAnswer = ruleBasedService.findAnswer(message);

        if (ruleAnswer.isPresent()) {
            return new ChatResponse(ruleAnswer.get(), ResponseSource.RULE);
        }

        if (!geminiClient.isConfigured()) {
            return new ChatResponse(NOT_UNDERSTOOD, ResponseSource.FALLBACK);
        }

        return geminiClient.generate(SYSTEM_INSTRUCTION, message)
                .map(aiAnswer -> new ChatResponse(aiAnswer, ResponseSource.AI))
                .orElseGet(() -> new ChatResponse(AI_UNAVAILABLE, ResponseSource.FALLBACK));
    }
}