package com.energymanagement.customersupportservice.controller;

import com.energymanagement.customersupportservice.dto.ChatRequest;
import com.energymanagement.customersupportservice.dto.ChatResponse;
import com.energymanagement.customersupportservice.service.RuleBasedSupportService;
import com.energymanagement.customersupportservice.service.AiSupportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/support")
@Tag(name = "Customer Support", description = "Customer support chatbot")
public class SupportController
{

    private final RuleBasedSupportService ruleBasedService;
    private final AiSupportService aiService;

    public SupportController(RuleBasedSupportService ruleBasedService, AiSupportService aiService)
    {
        this.ruleBasedService = ruleBasedService;
        this.aiService = aiService;
    }

    @Operation(summary = "Trimite mesaj la chatbot")
    @PostMapping("/message")
    public ResponseEntity<ChatResponse> handleMessage(@RequestBody ChatRequest request)
    {
        System.out.println("Received message: " + request);

        // Validare
        if (request.getMessage() == null || request.getMessage().trim().isEmpty())
        {
            return ResponseEntity.badRequest()
                    .body(new ChatResponse("Mesajul nu poate fi gol.", true, "ERROR"));
        }

        // 1. Rule-based check
        String ruleResponse = ruleBasedService.processMessage(request.getMessage());

        if (ruleResponse != null)
        {
            System.out.println("Rule matched: " + ruleResponse);
            return ResponseEntity.ok(new ChatResponse(ruleResponse, true, "RULE"));
        }

        // 2. AI-driven response
        System.out.println("No rule matched - calling AI service");
        String aiResponse = aiService.generateResponse(request.getMessage());
        System.out.println("AI response: " + aiResponse);

        return ResponseEntity.ok(new ChatResponse(aiResponse, true, "AI"));
    }


    @Operation(summary = "Health check")
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck()
    {
        return ResponseEntity.ok("Customer Support Service is running!");
    }
}