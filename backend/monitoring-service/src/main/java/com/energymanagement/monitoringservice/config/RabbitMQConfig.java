package com.energymanagement.monitoringservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitMQConfig
{

    // Data Broker Configuration
    @Value("${spring.rabbitmq.data.host}")
    private String dataHost;

    @Value("${spring.rabbitmq.data.port}")
    private int dataPort;

    @Value("${spring.rabbitmq.data.username}")
    private String dataUsername;

    @Value("${spring.rabbitmq.data.password}")
    private String dataPassword;


    // Sync Broker Configuration
    @Value("${spring.rabbitmq.sync.host}")
    private String syncHost;

    @Value("${spring.rabbitmq.sync.port}")
    private int syncPort;

    @Value("${spring.rabbitmq.sync.username}")
    private String syncUsername;

    @Value("${spring.rabbitmq.sync.password}")
    private String syncPassword;

    // Queue names
    @Value("${rabbitmq.queue.data}")
    private String dataQueueName;

    @Value("${rabbitmq.queue.sync}")
    private String syncQueueName;

    // DATA BROKER - Connection Factory
    @Bean
    @Primary
    @Qualifier("dataConnectionFactory")
    public ConnectionFactory dataConnectionFactory()
    {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(dataHost);
        factory.setPort(dataPort);
        factory.setUsername(dataUsername);
        factory.setPassword(dataPassword);
        return factory;
    }


    // SYNC BROKER - Connection Factory
    @Bean
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

    // QUEUE DEFINITIONS

    // Device Data Queue - pe Data Broker
    @Bean
    public Queue deviceDataQueue()
    {
        return new Queue(dataQueueName, true);
    }

    // Notifications Queue - pe Sync Broker
    @Bean
    public Queue notificationsQueue()
    {
        return new Queue("notifications-queue", true);
    }

    // RABBIT ADMIN - Pentru Data Broker
    @Bean
    public RabbitAdmin dataRabbitAdmin(@Qualifier("dataConnectionFactory") ConnectionFactory dataConnectionFactory)
    {
        RabbitAdmin admin = new RabbitAdmin(dataConnectionFactory);
        admin.declareQueue(deviceDataQueue());
        return admin;
    }

    // RABBIT ADMIN - Pentru Sync Broker
    @Bean
    public RabbitAdmin syncRabbitAdmin(@Qualifier("syncConnectionFactory") ConnectionFactory syncConnectionFactory)
    {
        RabbitAdmin admin = new RabbitAdmin(syncConnectionFactory);
        admin.declareQueue(notificationsQueue());
        return admin;
    }

    // MESSAGE CONVERTER
    @Bean
    public MessageConverter jsonMessageConverter()
    {
        return new Jackson2JsonMessageConverter();
    }

    // RABBIT TEMPLATES
    @Bean
    @Primary
    @Qualifier("dataRabbitTemplate")
    public RabbitTemplate dataRabbitTemplate(@Qualifier("dataConnectionFactory") ConnectionFactory connectionFactory)
    {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }


    @Bean
    @Qualifier("syncRabbitTemplate")
    public RabbitTemplate syncRabbitTemplate(@Qualifier("syncConnectionFactory") ConnectionFactory connectionFactory)
    {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }




    // LISTENER CONTAINER FACTORIES

    @Bean
    public org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            @Qualifier("dataConnectionFactory") ConnectionFactory connectionFactory
    ) {
        org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory factory =
                new org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setMissingQueuesFatal(false);
        return factory;
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