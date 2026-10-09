package com.energymanagement.usermanagement.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;

// Publishes USER_DELETED on the fanout sync exchange. USER_CREATED is published by the Authorization Service.
@Component
public class UserEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(UserEventPublisher.class);

    private final RabbitTemplate syncRabbitTemplate;

    @Value("${rabbitmq.exchange.sync}")
    private String syncExchangeName;

    public UserEventPublisher(RabbitTemplate syncRabbitTemplate) {
        this.syncRabbitTemplate = syncRabbitTemplate;
    }

    public void userDeleted(Long userId) {
        Map<String, Object> event = Map.of("eventType", "USER_DELETED", "userId", userId);

        // Inside a transaction, the event is sent only after the commit succeeds
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(event, userId);
                }
            });
        } else {
            send(event, userId);
        }
    }

    private void send(Map<String, Object> event, Long userId) {
        try {
            syncRabbitTemplate.convertAndSend(syncExchangeName, "", event);
            log.info("Published USER_DELETED for user {}", userId);
        } catch (AmqpException e) {
            log.error("Failed to publish USER_DELETED for user {}", userId, e);
        }
    }
}