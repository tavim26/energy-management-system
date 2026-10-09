package com.energymanagement.authorizationservice.config;

import com.energymanagement.authorizationservice.dto.AdminCreateUserDTO;
import com.energymanagement.authorizationservice.exception.ConflictException;
import com.energymanagement.authorizationservice.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// Public registration can only create CLIENT accounts, so the first ADMIN
// is created here on startup, from environment variables
@Component
public class AdminBootstrap {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private static final int MAX_ATTEMPTS = 10;
    private static final long RETRY_DELAY_MS = 5000;

    private final AuthService authService;
    private final String username;
    private final String password;

    public AdminBootstrap(
            AuthService authService,
            @Value("${admin.bootstrap.username}") String username,
            @Value("${admin.bootstrap.password}") String password
    ) {
        this.authService = authService;
        this.username = username;
        this.password = password;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createInitialAdmin() {
        if (username.isBlank() || password.isBlank()) {
            log.warn("ADMIN_USERNAME / ADMIN_PASSWORD not set - initial admin not created");
            return;
        }

        if (authService.adminExists()) {
            return;
        }

        AdminCreateUserDTO request = new AdminCreateUserDTO(
                username, password, "Administrator", null, AuthService.ROLE_ADMIN);

        // The User Service may still be starting, so the call is retried
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                authService.createUserAsAdmin(request);
                log.info("Initial admin account created: {}", username);
                return;

            } catch (ConflictException e) {
                log.error("Initial admin not created: {}", e.getMessage());
                return;

            } catch (RuntimeException e) {
                log.warn("Initial admin creation failed (attempt {}/{}): {}", attempt, MAX_ATTEMPTS, e.getMessage());
            }

            try {
                Thread.sleep(RETRY_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}