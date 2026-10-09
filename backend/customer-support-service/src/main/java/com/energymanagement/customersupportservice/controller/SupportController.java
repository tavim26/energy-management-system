package com.energymanagement.customersupportservice.controller;

import com.energymanagement.customersupportservice.dto.ChatRequest;
import com.energymanagement.customersupportservice.dto.ChatResponse;
import com.energymanagement.customersupportservice.service.SupportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/support")
@Tag(name = "Customer Support", description = "Customer support chatbot")
public class SupportController {

    private final SupportService supportService;

    public SupportController(SupportService supportService) {
        this.supportService = supportService;
    }

    @Operation(summary = "Send a message to the chatbot")
    @PostMapping("/message")
    public ChatResponse sendMessage(@Valid @RequestBody ChatRequest request) {
        return supportService.answer(request.message());
    }

    @Operation(summary = "Health check")
    @GetMapping("/health")
    public String healthCheck() {
        return "Customer Support Service is running";
    }
}