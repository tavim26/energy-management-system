package com.energymanagement.usermanagement.config;

import org.springframework.amqp.core.*;
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

/**
 * Configuratie RabbitMQ pentru Sync Broker
 *
 * User Service este responsabil pentru declararea INTREGII infrastructuri de sincronizare:
 * - sync-exchange (Fanout Exchange pentru broadcast)
 * - sync-queue-device (pentru Device Service)
 * - sync-queue-monitoring (pentru Monitoring Service)
 * - sync-queue-auth (pentru Authorization Service)
 * - binding-uri pentru toate queue-urile
 *
 * Celelalte servicii (Auth, Device, Monitoring) se conecteaza doar la aceasta infrastructura,
 */
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

    // CONNECTION FACTORY
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

    // FANOUT EXCHANGE - pentru broadcast la toate serviciile
    @Bean
    public FanoutExchange syncExchange()
    {
        return new FanoutExchange(syncExchangeName, true, false);
    }

    // QUEUE 1: Pentru Device Service
    @Bean
    public Queue syncQueueDevice()
    {
        return new Queue("sync-queue-device", true);
    }

    // QUEUE 2: Pentru Monitoring Service
    @Bean
    public Queue syncQueueMonitoring()
    {
        return new Queue("sync-queue-monitoring", true);
    }

    // QUEUE 3: Pentru Authorization Service
    @Bean
    public Queue syncQueueAuth()
    {
        return new Queue("sync-queue-auth", true);
    }

    // BINDING 1: Device Queue -> Exchange
    @Bean
    public Binding bindingSyncDevice(Queue syncQueueDevice, FanoutExchange syncExchange)
    {
        return BindingBuilder.bind(syncQueueDevice).to(syncExchange);
    }

    // BINDING 2: Monitoring Queue -> Exchange
    @Bean
    public Binding bindingSyncMonitoring(Queue syncQueueMonitoring, FanoutExchange syncExchange)
    {
        return BindingBuilder.bind(syncQueueMonitoring).to(syncExchange);
    }

    // BINDING 3: Auth Queue -> Exchange
    @Bean
    public Binding bindingSyncAuth(Queue syncQueueAuth, FanoutExchange syncExchange)
    {
        return BindingBuilder.bind(syncQueueAuth).to(syncExchange);
    }

    // RABBIT ADMIN - declara toata infrastructura la pornirea serviciului
    @Bean
    public RabbitAdmin syncRabbitAdmin(@Qualifier("syncConnectionFactory") ConnectionFactory syncConnectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(syncConnectionFactory);

        // Declara exchange-ul
        admin.declareExchange(syncExchange());

        // Declara toate queue-urile
        admin.declareQueue(syncQueueDevice());
        admin.declareQueue(syncQueueMonitoring());
        admin.declareQueue(syncQueueAuth());

        // Declara toate binding-urile
        admin.declareBinding(bindingSyncDevice(syncQueueDevice(), syncExchange()));
        admin.declareBinding(bindingSyncMonitoring(syncQueueMonitoring(), syncExchange()));
        admin.declareBinding(bindingSyncAuth(syncQueueAuth(), syncExchange()));

        System.out.println("RabbitMQ Sync Infrastructure declared by User Service:");
        System.out.println("  - Exchange: " + syncExchangeName + " (Fanout)");
        System.out.println("  - Queue: sync-queue-device");
        System.out.println("  - Queue: sync-queue-monitoring");
        System.out.println("  - Queue: sync-queue-auth");

        return admin;
    }

    // MESSAGE CONVERTER
    @Bean
    public MessageConverter jsonMessageConverter()
    {
        return new Jackson2JsonMessageConverter();
    }

    // RABBIT TEMPLATE - pentru publicare evenimente
    @Bean
    @Primary
    @Qualifier("syncRabbitTemplate")
    public RabbitTemplate syncRabbitTemplate(@Qualifier("syncConnectionFactory") ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}