package com.energymanagement.authorizationservice.service;

import com.energymanagement.authorizationservice.repository.CredentialRepository;
import com.energymanagement.authorizationservice.model.Credential;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;

// Consumer pentru evenimente de sincronizare
// Asculta pe Sync Queue si reactioneaza la evenimente USER_DELETED

@Component
public class UserSyncConsumer
{

    private final CredentialRepository credentialRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserSyncConsumer(CredentialRepository credentialRepository)
    {
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @RabbitListener(
            queues = "sync-queue-auth",
            containerFactory = "syncRabbitListenerContainerFactory"
    )
    public void consumeSyncEvent(Map<String, Object> message)
    {
        try {
            String eventType = (String) message.get("eventType");

            System.out.println("Received sync event in Auth Service: " + message);

            if ("USER_CREATED".equals(eventType))
            {
                handleUserCreated(message);

            }
            else if ("USER_DELETED".equals(eventType))
            {
                handleUserDeleted(message);
            }

        } catch (Exception e) {
            System.err.println("Error processing sync event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleUserCreated(Map<String, Object> message)
    {
        try {
            Long userId = ((Number) message.get("userId")).longValue();

            // verifica dacă credențialele sunt deja
            if (credentialRepository.existsById(userId))
            {
                System.out.println("Credentials already exist for user ID: " + userId);
                return;
            }

            String username = (String) message.get("username");
            String password = (String) message.get("password");
            String role = (String) message.get("role");


            if (username == null || password == null)
            {
                System.err.println("Cannot create credentials: missing username or password");
                return;
            }

            // Hash parola
            String hashedPassword = passwordEncoder.encode(password);

            // creare credential
            Credential credential = new Credential();
            credential.setId(userId);
            credential.setUsername(username);
            credential.setPasswordHash(hashedPassword);
            credential.setRole(role != null ? role : "CLIENT");

            credentialRepository.save(credential);

            System.out.println("Credentials created for user ID: " + userId);

        } catch (Exception e) {
            System.err.println("Error creating credentials from sync event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleUserDeleted(Map<String, Object> message)
    {
        try {
            Long userId = ((Number) message.get("userId")).longValue();
            System.out.println("User deleted with ID: " + userId);

            if (credentialRepository.existsById(userId))
            {
                credentialRepository.deleteById(userId);
                System.out.println("Credentials deleted for user ID: " + userId);
            }
            else
            {
                System.out.println("Credentials not found for user ID: " + userId);
            }

        } catch (Exception e) {

            System.err.println("Error deleting credentials: " + e.getMessage());
            e.printStackTrace();
        }
    }
}