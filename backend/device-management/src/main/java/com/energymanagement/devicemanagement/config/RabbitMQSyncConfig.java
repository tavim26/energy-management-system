package com.energymanagement.devicemanagement.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitMQSyncConfig
{

    @Value("${spring.rabbitmq.sync.host}")
    private String syncHost;

    @Value("${spring.rabbitmq.sync.port}")
    private int syncPort;

    @Value("${spring.rabbitmq.sync.username}")
    private String syncUsername;

    @Value("${spring.rabbitmq.sync.password}")
    private String syncPassword;

    @Value("${rabbitmq.exchange.sync}")
    private String syncExchangeName;

    @Bean
    @Primary
    @Qualifier("syncConnectionFactory")
    public ConnectionFactory syncConnectionFactory()
    {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(syncHost);
        factory.setPort(syncPort);
        factory.setUsername(syncUsername);
        factory.setPassword(syncPassword);
        return factory;
    }

    @Bean
    public MessageConverter jsonMessageConverter()
    {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @Primary
    @Qualifier("syncRabbitTemplate")
    public RabbitTemplate syncRabbitTemplate(
            @Qualifier("syncConnectionFactory") ConnectionFactory connectionFactory)
    {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }


    @Bean
    public org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory syncRabbitListenerContainerFactory(
            @Qualifier("syncConnectionFactory") ConnectionFactory connectionFactory
    ) {
        org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory factory =
                new org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setMissingQueuesFatal(false);
        return factory;
    }
}