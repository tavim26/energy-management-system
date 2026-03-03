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

@Service
public class AuthService
{

    private final CredentialRepository credentialRepository;
    private final UserServiceClient userServiceClient;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    // RabbitTemplate pentru publicare evenimente de sincronizare
    private final RabbitTemplate syncRabbitTemplate;

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
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // Register
    // 1. Apeleaza User Service sa creeze user-ul (REST - SINCRON)
    // 2. Creeaza credential cu acelasi ID
    // 3. Publica eveniment USER_CREATED pe Sync Exchange (ASINCRON)
    public RegisterResponseDTO register(RegisterRequestDTO registerRequestDTO)
    {
        // Validari
        if (credentialRepository.existsByUsername(registerRequestDTO.getUsername()))
        {
            throw new RuntimeException("Username already exists: " + registerRequestDTO.getUsername());
        }

        if (registerRequestDTO.getUsername() == null || registerRequestDTO.getUsername().trim().isEmpty())
        {
            throw new RuntimeException("Username cannot be empty");
        }

        if (registerRequestDTO.getPassword() == null || registerRequestDTO.getPassword().trim().isEmpty())
        {
            throw new RuntimeException("Password cannot be empty");
        }

        String role = (registerRequestDTO.getRole() != null && !registerRequestDTO.getRole().trim().isEmpty())
                ? registerRequestDTO.getRole()
                : "CLIENT";

        // 1. Apeleaza User Service pentru a crea user
        Long userId = userServiceClient.createUserAndGetId(
                registerRequestDTO.getFullName(),
                registerRequestDTO.getAddress()
        );

        // 2. Hash parola
        String hashedPassword = passwordEncoder.encode(registerRequestDTO.getPassword());

        // 3. Creeaza credential cu acelasi ID
        Credential credential = new Credential();
        credential.setId(userId);
        credential.setUsername(registerRequestDTO.getUsername());
        credential.setPasswordHash(hashedPassword);
        credential.setRole(role);

        Credential savedCredential = credentialRepository.save(credential);

        // 4. Publica eveniment USER_CREATED pe Sync Exchange (ASINCRON)
        publishUserCreatedEvent(
                userId,
                registerRequestDTO.getUsername(),
                registerRequestDTO.getPassword(),
                role,
                registerRequestDTO.getFullName(),
                registerRequestDTO.getAddress()
        );

        return new RegisterResponseDTO(
                savedCredential.getId(),
                savedCredential.getUsername(),
                savedCredential.getRole(),
                registerRequestDTO.getFullName(),
                registerRequestDTO.getAddress(),
                "User registered successfully"
        );
    }

    // Login - autentificare user si returneaza JWT token
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO)
    {
        if (loginRequestDTO.getUsername() == null || loginRequestDTO.getUsername().trim().isEmpty())
        {
            throw new RuntimeException("Username cannot be empty");
        }

        if (loginRequestDTO.getPassword() == null || loginRequestDTO.getPassword().trim().isEmpty())
        {
            throw new RuntimeException("Password cannot be empty");
        }

        // Cauta credential dupa username
        Optional<Credential> credentialOptional = credentialRepository.findByUsername(loginRequestDTO.getUsername());

        if (!credentialOptional.isPresent())
        {
            throw new RuntimeException("Invalid username or password");
        }

        Credential credential = credentialOptional.get();

        // Verifica parola
        boolean passwordMatches = passwordEncoder.matches(
                loginRequestDTO.getPassword(),
                credential.getPasswordHash()
        );

        if (!passwordMatches)
        {
            throw new RuntimeException("Invalid username or password");
        }

        // Genereaza JWT token
        String token = jwtService.generateToken(
                credential.getId(),
                credential.getUsername(),
                credential.getRole()
        );

        return new LoginResponseDTO(
                token,
                credential.getId(),
                credential.getUsername(),
                credential.getRole()
        );
    }


    // Publica eveniment USER_CREATED pe Sync Exchange
    private void publishUserCreatedEvent(Long userId, String username, String password, String role, String fullName, String address)
    {
        try {
            Map<String, Object> syncMessage = new HashMap<>();
            syncMessage.put("eventType", "USER_CREATED");
            syncMessage.put("userId", userId);
            syncMessage.put("username", username);
            syncMessage.put("password", password);
            syncMessage.put("role", role);
            syncMessage.put("fullName", fullName);
            syncMessage.put("address", address);

            // Publica pe Sync Exchange (fanout broadcast)
            syncRabbitTemplate.convertAndSend(syncExchangeName, "", syncMessage);

            System.out.println("Published USER_CREATED event for user ID: " + userId);

        } catch (Exception e) {

            System.err.println("Failed to publish USER_CREATED event: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Returneaza username si role pentru un user
    public Map<String, String> getUserCredentials(Long userId)
    {
        Optional<Credential> credentialOptional = credentialRepository.findById(userId);

        if (credentialOptional.isPresent())
        {
            Credential credential = credentialOptional.get();

            Map<String, String> result = new HashMap<>();
            result.put("username", credential.getUsername());
            result.put("role", credential.getRole());

            return result;
        }

        return null;
    }
}