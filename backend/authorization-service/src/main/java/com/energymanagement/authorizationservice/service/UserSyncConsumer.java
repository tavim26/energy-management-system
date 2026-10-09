package com.energymanagement.authorizationservice.service;

import com.energymanagement.authorizationservice.repository.CredentialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

// Credentials are created directly by AuthService, so this consumer
// only reacts to USER_DELETED events published by the User Service
@Component
public class UserSyncConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserSyncConsumer.class);

    private final CredentialRepository credentialRepository;

    public UserSyncConsumer(CredentialRepository credentialRepository) {
        this.credentialRepository = credentialRepository;
    }

    @RabbitListener(queues = "${rabbitmq.queue.sync}", containerFactory = "syncRabbitListenerContainerFactory")
    public void consumeSyncEvent(Map<String, Object> message) {
        if (!"USER_DELETED".equals(message.get("eventType"))) {
            return;
        }

        try {
            Long userId = ((Number) message.get("userId")).longValue();

            if (credentialRepository.existsById(userId)) {
                credentialRepository.deleteById(userId);
                log.info("Credentials deleted for user {}", userId);
            }

        } catch (Exception e) {
            // The message is dropped instead of being redelivered forever
            log.error("Failed to process USER_DELETED event {}", message, e);
        }
    }
}