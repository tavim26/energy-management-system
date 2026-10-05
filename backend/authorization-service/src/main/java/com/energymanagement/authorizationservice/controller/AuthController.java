package com.energymanagement.authorizationservice.controller;

import com.energymanagement.authorizationservice.dto.*;
import com.energymanagement.authorizationservice.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.energymanagement.authorizationservice.service.JwtService;
import org.springframework.http.HttpHeaders;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController
{

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService)
    {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@RequestBody RegisterRequestDTO registerRequestDTO)
    {
        try {
            RegisterResponseDTO response = authService.register(registerRequestDTO);

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (RuntimeException e)
        {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO)
    {
        try {
            LoginResponseDTO response = authService.login(loginRequestDTO);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e)
        {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }

    // POST /api/auth/users - account creation from the admin panel
    @PostMapping("/users")
    public ResponseEntity<RegisterResponseDTO> createUser(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestBody AdminCreateUserDTO request)
    {
        if (!isAdmin(authHeader))
        {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            return new ResponseEntity<>(authService.createUserAsAdmin(request), HttpStatus.CREATED);

        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // The gateway already checks the role; checking it here too keeps the endpoint
    // safe when the service is called directly, without going through the gateway
    private boolean isAdmin(String authHeader)
    {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
        {
            return false;
        }

        return AuthService.ROLE_ADMIN.equals(jwtService.extractRole(authHeader.substring(7)));
    }


    // GET /api/auth/credentials/{userId}
    // Returneaza username si role pentru un user (apelat de User Service)
    @GetMapping("/credentials/{userId}")
    public ResponseEntity<Map<String, String>> getUserCredentials(@PathVariable Long userId)
    {
        try {
            Map<String, String> credentials = authService.getUserCredentials(userId);

            if (credentials != null)
            {
                return ResponseEntity.ok(credentials);
            }
            else 
            {
                return ResponseEntity.notFound().build();
            }

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

}