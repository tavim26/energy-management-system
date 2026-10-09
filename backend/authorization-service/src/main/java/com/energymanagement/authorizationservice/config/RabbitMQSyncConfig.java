package com.energymanagement.authorizationservice.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Connection to the sync broker. The exchange and queues are declared by the User Service.
@Configuration
public class RabbitMQSyncConfig {

    @Bean
    public ConnectionFactory syncConnectionFactory(
            @Value("${spring.rabbitmq.sync.host}") String host,
            @Value("${spring.rabbitmq.sync.port}") int port,
            @Value("${spring.rabbitmq.sync.username}") String username,
            @Value("${spring.rabbitmq.sync.password}") String password
    ) {
        CachingConnectionFactory factory = new CachingConnectionFactory(host, port);
        factory.setUsername(username);
        factory.setPassword(password);
        return factory;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate syncRabbitTemplate(ConnectionFactory syncConnectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(syncConnectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory syncRabbitListenerContainerFactory(
            ConnectionFactory syncConnectionFactory,
            MessageConverter jsonMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(syncConnectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setMissingQueuesFatal(false);
        return factory;
    }
}