package com.energymanagement.authorizationservice.config;

import com.energymanagement.authorizationservice.dto.AdminCreateUserDTO;
import com.energymanagement.authorizationservice.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// Public registration can only create CLIENT accounts, so the first ADMIN
// is created here on startup, from environment variables
@Component
public class AdminBootstrap
{
    private static final int MAX_ATTEMPTS = 10;
    private static final long RETRY_DELAY_MS = 5000;

    private final AuthService authService;

    @Value("${admin.bootstrap.username}")
    private String username;

    @Value("${admin.bootstrap.password}")
    private String password;

    public AdminBootstrap(AuthService authService)
    {
        this.authService = authService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createInitialAdmin()
    {
        if (username.isBlank() || password.isBlank())
        {
            System.out.println("ADMIN_USERNAME / ADMIN_PASSWORD not set - initial admin not created");
            return;
        }

        if (authService.adminExists())
        {
            return;
        }

        AdminCreateUserDTO request = new AdminCreateUserDTO();
        request.setUsername(username);
        request.setPassword(password);
        request.setRole(AuthService.ROLE_ADMIN);
        request.setFullName("Administrator");

        // User Service may still be starting, so the call is retried
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++)
        {
            try {
                authService.createUserAsAdmin(request);
                System.out.println("Initial admin account created: " + username);
                return;

            } catch (RuntimeException e) {
                System.err.println("Initial admin creation failed (attempt " + attempt + "/" + MAX_ATTEMPTS + "): " + e.getMessage());
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