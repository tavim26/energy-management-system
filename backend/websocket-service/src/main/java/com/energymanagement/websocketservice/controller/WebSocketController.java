package com.energymanagement.websocketservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/websocket")
@Tag(name = "WebSocket Service", description = "WebSocket service for real-time notifications")
public class WebSocketController
{

    @Operation(summary = "Health check")
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck()
    {
        return ResponseEntity.ok("WebSocket Service is running!");
    }
}