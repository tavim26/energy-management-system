package com.energymanagement.usermanagement.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// The User Service declares the whole sync topology: a fanout exchange that copies
// every event into one queue per consuming service (device, monitoring, auth)
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
    public Declarables syncTopology(@Value("${rabbitmq.exchange.sync}") String exchangeName) {
        FanoutExchange exchange = new FanoutExchange(exchangeName, true, false);
        Queue deviceQueue = new Queue("sync-queue-device", true);
        Queue monitoringQueue = new Queue("sync-queue-monitoring", true);
        Queue authQueue = new Queue("sync-queue-auth", true);

        return new Declarables(
                exchange, deviceQueue, monitoringQueue, authQueue,
                BindingBuilder.bind(deviceQueue).to(exchange),
                BindingBuilder.bind(monitoringQueue).to(exchange),
                BindingBuilder.bind(authQueue).to(exchange)
        );
    }

    @Bean
    public RabbitAdmin syncRabbitAdmin(ConnectionFactory syncConnectionFactory) {
        return new RabbitAdmin(syncConnectionFactory);
    }

    // RabbitAdmin normally declares the topology on the first connection, which here would only
    // happen at the first USER_DELETED. The other services need it right away, so it is declared on startup.
    @Bean
    public ApplicationRunner declareSyncTopology(RabbitAdmin syncRabbitAdmin) {
        return args -> syncRabbitAdmin.initialize();
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
}