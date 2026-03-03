package com.energymanagement.websocketservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig
{

    @Value("${spring.rabbitmq.host:localhost}")
    private String host;

    @Value("${spring.rabbitmq.port:5672}")
    private int port;

    @Value("${spring.rabbitmq.username:guest}")
    private String username;

    @Value("${spring.rabbitmq.password:guest}")
    private String password;

    @Value("${rabbitmq.queue.notifications:notifications-queue}")
    private String notificationsQueueName;

    // Connection Factory
    @Bean
    public ConnectionFactory connectionFactory()
    {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);

        System.out.println("RabbitMQ Config - Host: " + host + ", Port: " + port);

        return factory;
    }

    // Queue declaration
    @Bean
    public Queue notificationsQueue()
    {
        return new Queue(notificationsQueueName, true);
    }

    // RabbitAdmin pentru declarare queue
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory)
    {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.declareQueue(notificationsQueue());

        System.out.println("RabbitMQ Queue declared: " + notificationsQueueName);

        return admin;
    }

    // Message Converter - NECESAR pentru RabbitListener!
    @Bean
    public MessageConverter jsonMessageConverter()
    {
        return new Jackson2JsonMessageConverter(new ObjectMapper());
    }
}