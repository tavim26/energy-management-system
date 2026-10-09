package com.energymanagement.authorizationservice.controller;

import com.energymanagement.authorizationservice.dto.*;
import com.energymanagement.authorizationservice.exception.ForbiddenException;
import com.energymanagement.authorizationservice.service.AuthService;
import com.energymanagement.authorizationservice.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }

    // Account creation from the admin panel
    @PostMapping("/users")
    public ResponseEntity<RegisterResponseDTO> createUser(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody AdminCreateUserDTO request
    ) {
        ensureAdmin(authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createUserAsAdmin(request));
    }

    // The gateway already checks the role; checking it here too keeps the endpoint
    // safe when the service is called directly, without going through the gateway
    private void ensureAdmin(String authHeader) {
        boolean isAdmin = authHeader != null
                && authHeader.startsWith("Bearer ")
                && AuthService.ROLE_ADMIN.equals(jwtService.extractRole(authHeader.substring(7)));

        if (!isAdmin) {
            throw new ForbiddenException("Only administrators can create accounts");
        }
    }
}