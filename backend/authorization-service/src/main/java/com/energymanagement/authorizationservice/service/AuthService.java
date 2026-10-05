package com.energymanagement.authorizationservice.service;

import com.energymanagement.authorizationservice.dto.*;
import com.energymanagement.authorizationservice.model.Credential;
import com.energymanagement.authorizationservice.repository.CredentialRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthService
{
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CLIENT = "CLIENT";
    private static final Set<String> VALID_ROLES = Set.of(ROLE_ADMIN, ROLE_CLIENT);

    private final CredentialRepository credentialRepository;
    private final UserServiceClient userServiceClient;
    private final JwtService jwtService;
    private final RabbitTemplate syncRabbitTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${rabbitmq.exchange.sync:sync-exchange}")
    private String syncExchangeName;

    public AuthService(
            CredentialRepository credentialRepository,
            JwtService jwtService,
            UserServiceClient userServiceClient,
            @Qualifier("syncRabbitTemplate") RabbitTemplate syncRabbitTemplate
    ) {
        this.credentialRepository = credentialRepository;
        this.jwtService = jwtService;
        this.userServiceClient = userServiceClient;
        this.syncRabbitTemplate = syncRabbitTemplate;
    }

    // Public self-registration always creates a CLIENT account
    public RegisterResponseDTO register(RegisterRequestDTO request)
    {
        return createAccount(request, ROLE_CLIENT);
    }

    // Account creation from the admin panel, where the role can be chosen
    public RegisterResponseDTO createUserAsAdmin(AdminCreateUserDTO request)
    {
        String role = (request.getRole() == null || request.getRole().isBlank())
                ? ROLE_CLIENT
                : request.getRole().trim().toUpperCase();

        if (!VALID_ROLES.contains(role))
        {
            throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }

        return createAccount(request, role);
    }

    public boolean adminExists()
    {
        return credentialRepository.existsByRole(ROLE_ADMIN);
    }

    public LoginResponseDTO login(LoginRequestDTO request)
    {
        if (isBlank(request.getUsername()) || isBlank(request.getPassword()))
        {
            throw new IllegalArgumentException("Username and password are required");
        }

        Optional<Credential> credentialOptional = credentialRepository.findByUsername(request.getUsername());

        if (credentialOptional.isEmpty()
                || !passwordEncoder.matches(request.getPassword(), credentialOptional.get().getPasswordHash()))
        {
            throw new IllegalArgumentException("Invalid username or password");
        }

        Credential credential = credentialOptional.get();

        String token = jwtService.generateToken(
                credential.getId(),
                credential.getUsername(),
                credential.getRole()
        );

        return new LoginResponseDTO(token, credential.getId(), credential.getUsername(), credential.getRole());
    }

    public Map<String, String> getUserCredentials(Long userId)
    {
        Optional<Credential> credentialOptional = credentialRepository.findById(userId);

        if (credentialOptional.isEmpty())
        {
            return null;
        }

        Credential credential = credentialOptional.get();

        Map<String, String> result = new HashMap<>();
        result.put("username", credential.getUsername());
        result.put("role", credential.getRole());

        return result;
    }

    // 1. User Service creates the user profile (REST, synchronous) and returns its id
    // 2. The hashed password is stored here, under the same id
    // 3. USER_CREATED is published so the other services can sync (asynchronous)
    private RegisterResponseDTO createAccount(RegisterRequestDTO request, String role)
    {
        if (isBlank(request.getUsername()))
        {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        if (isBlank(request.getPassword()))
        {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        if (credentialRepository.existsByUsername(request.getUsername()))
        {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        Long userId = userServiceClient.createUserAndGetId(request.getFullName(), request.getAddress());

        Credential credential = new Credential(
                userId,
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                role
        );
        credentialRepository.save(credential);

        publishUserCreatedEvent(userId, request.getUsername(), role);

        return new RegisterResponseDTO(
                userId,
                request.getUsername(),
                role,
                request.getFullName(),
                request.getAddress(),
                "User registered successfully"
        );
    }

    // The password never leaves this service: other services only need to know the user exists
    private void publishUserCreatedEvent(Long userId, String username, String role)
    {
        try {
            Map<String, Object> syncMessage = new HashMap<>();
            syncMessage.put("eventType", "USER_CREATED");
            syncMessage.put("userId", userId);
            syncMessage.put("username", username);
            syncMessage.put("role", role);

            syncRabbitTemplate.convertAndSend(syncExchangeName, "", syncMessage);

            System.out.println("Published USER_CREATED event for user ID: " + userId);

        } catch (Exception e) {
            System.err.println("Failed to publish USER_CREATED event: " + e.getMessage());
        }
    }

    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }
}