package com.energymanagement.authorizationservice.service;

import com.energymanagement.authorizationservice.client.UserServiceClient;
import com.energymanagement.authorizationservice.dto.*;
import com.energymanagement.authorizationservice.event.UserEventPublisher;
import com.energymanagement.authorizationservice.exception.ConflictException;
import com.energymanagement.authorizationservice.exception.InvalidCredentialsException;
import com.energymanagement.authorizationservice.exception.ResourceNotFoundException;
import com.energymanagement.authorizationservice.model.Credential;
import com.energymanagement.authorizationservice.repository.CredentialRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CLIENT = "CLIENT";

    private final CredentialRepository credentialRepository;
    private final UserServiceClient userServiceClient;
    private final UserEventPublisher eventPublisher;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(
            CredentialRepository credentialRepository,
            UserServiceClient userServiceClient,
            UserEventPublisher eventPublisher,
            JwtService jwtService
    ) {
        this.credentialRepository = credentialRepository;
        this.userServiceClient = userServiceClient;
        this.eventPublisher = eventPublisher;
        this.jwtService = jwtService;
    }

    // Public self-registration always creates a CLIENT account
    public RegisterResponseDTO register(RegisterRequestDTO request) {
        return createAccount(request.username(), request.password(), request.fullName(), request.address(), ROLE_CLIENT);
    }

    // Account creation from the admin panel, where the role can be chosen
    public RegisterResponseDTO createUserAsAdmin(AdminCreateUserDTO request) {
        String role = request.role() == null ? ROLE_CLIENT : request.role();
        return createAccount(request.username(), request.password(), request.fullName(), request.address(), role);
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        Credential credential = credentialRepository.findByUsername(request.username().trim())
                .filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        String token = jwtService.generateToken(credential.getId(), credential.getUsername(), credential.getRole());

        return new LoginResponseDTO(token, credential.getId(), credential.getUsername(), credential.getRole());
    }

    @Transactional(readOnly = true)
    public CredentialDTO getCredentials(Long userId) {
        return credentialRepository.findById(userId)
                .map(CredentialDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for user " + userId));
    }

    @Transactional(readOnly = true)
    public List<CredentialDTO> getAllCredentials() {
        return credentialRepository.findAll().stream()
                .map(CredentialDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean adminExists() {
        return credentialRepository.existsByRole(ROLE_ADMIN);
    }

    // 1. The User Service creates the profile (full name, address) and returns the new user id
    // 2. The credentials are saved here, under the same id
    // 3. USER_CREATED is published so the other services can sync
    private RegisterResponseDTO createAccount(String username, String password, String fullName, String address, String role) {
        String trimmedUsername = username.trim();

        if (credentialRepository.existsByUsername(trimmedUsername)) {
            throw new ConflictException("Username '" + trimmedUsername + "' is already taken");
        }

        Long userId = userServiceClient.createProfile(fullName, address);

        try {
            credentialRepository.saveAndFlush(
                    new Credential(userId, trimmedUsername, passwordEncoder.encode(password), role));

        } catch (RuntimeException e) {
            // Without credentials the profile would be a user that can never log in
            userServiceClient.deleteProfile(userId);

            // The username was taken by another request between the check above and the save
            if (e instanceof DataIntegrityViolationException) {
                throw new ConflictException("Username '" + trimmedUsername + "' is already taken");
            }

            throw e;
        }

        eventPublisher.userCreated(userId, trimmedUsername, role);

        return new RegisterResponseDTO(userId, trimmedUsername, role, fullName, address);
    }
}