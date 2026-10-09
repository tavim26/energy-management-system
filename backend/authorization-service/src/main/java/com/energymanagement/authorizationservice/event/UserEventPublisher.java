package com.energymanagement.authorizationservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

// Publishes account events on the fanout sync exchange, so every subscribed service receives them
@Component
public class UserEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(UserEventPublisher.class);

    private final RabbitTemplate syncRabbitTemplate;

    @Value("${rabbitmq.exchange.sync}")
    private String syncExchangeName;

    public UserEventPublisher(RabbitTemplate syncRabbitTemplate) {
        this.syncRabbitTemplate = syncRabbitTemplate;
    }

    // The password is never part of the event: other services only need to know the user exists
    public void userCreated(Long userId, String username, String role) {
        Map<String, Object> event = Map.of(
                "eventType", "USER_CREATED",
                "userId", userId,
                "username", username,
                "role", role
        );

        try {
            syncRabbitTemplate.convertAndSend(syncExchangeName, "", event);
            log.info("Published USER_CREATED for user {}", userId);
        } catch (AmqpException e) {
            log.error("Failed to publish USER_CREATED for user {}", userId, e);
        }
    }
}