package com.energymanagement.authorizationservice.controller;

import com.energymanagement.authorizationservice.dto.CredentialDTO;
import com.energymanagement.authorizationservice.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Called only by the User Service. The gateway has no route for /internal/**,
// so these endpoints cannot be reached through it.
@RestController
@RequestMapping("/internal/credentials")
public class InternalCredentialController {

    private final AuthService authService;

    public InternalCredentialController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public List<CredentialDTO> getAllCredentials() {
        return authService.getAllCredentials();
    }

    @GetMapping("/{userId}")
    public CredentialDTO getCredentials(@PathVariable Long userId) {
        return authService.getCredentials(userId);
    }
}