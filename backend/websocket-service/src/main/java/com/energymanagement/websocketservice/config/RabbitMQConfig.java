package com.energymanagement.websocketservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// The connection itself is created by Spring Boot from the spring.rabbitmq.* properties
@Configuration
public class RabbitMQConfig {

    // Declared here as well as in the Monitoring Service, so it exists whichever service starts first
    @Bean
    public Queue notificationsQueue(@Value("${rabbitmq.queue.notifications}") String name) {
        return new Queue(name, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}