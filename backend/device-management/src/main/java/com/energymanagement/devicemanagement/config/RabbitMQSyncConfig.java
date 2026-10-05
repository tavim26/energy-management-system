package com.energymanagement.devicemanagement.config;

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

    @Value("${spring.rabbitmq.sync.host}")
    private String syncHost;

    @Value("${spring.rabbitmq.sync.port}")
    private int syncPort;

    @Value("${spring.rabbitmq.sync.username}")
    private String syncUsername;

    @Value("${spring.rabbitmq.sync.password}")
    private String syncPassword;

    @Bean
    public ConnectionFactory syncConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(syncHost);
        factory.setPort(syncPort);
        factory.setUsername(syncUsername);
        factory.setPassword(syncPassword);
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