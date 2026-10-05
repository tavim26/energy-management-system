package com.energymanagement.authorizationservice.service;

import com.energymanagement.authorizationservice.repository.CredentialRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

// Credentials are created directly by AuthService, so this consumer
// only reacts to USER_DELETED events published by the User Service
@Component
public class UserSyncConsumer
{

    private final CredentialRepository credentialRepository;

    public UserSyncConsumer(CredentialRepository credentialRepository)
    {
        this.credentialRepository = credentialRepository;
    }

    @RabbitListener(
            queues = "sync-queue-auth",
            containerFactory = "syncRabbitListenerContainerFactory"
    )
    public void consumeSyncEvent(Map<String, Object> message)
    {
        if (!"USER_DELETED".equals(message.get("eventType")))
        {
            return;
        }

        try {
            Long userId = ((Number) message.get("userId")).longValue();

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
            System.err.println("Error processing USER_DELETED event: " + e.getMessage());
        }
    }
}