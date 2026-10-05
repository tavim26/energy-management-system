package com.energymanagement.monitoringservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
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

// The service talks to two brokers:
// - data broker: receives measurements from the device simulator
// - sync broker: receives device events and sends overconsumption alerts
@Configuration
public class RabbitMQConfig {

    // Primary because Spring Boot's own RabbitMQ setup expects a single default connection
    @Bean
    @Primary
    public ConnectionFactory dataConnectionFactory(
            @Value("${spring.rabbitmq.data.host}") String host,
            @Value("${spring.rabbitmq.data.port}") int port,
            @Value("${spring.rabbitmq.data.username}") String username,
            @Value("${spring.rabbitmq.data.password}") String password
    ) {
        return connectionFactory(host, port, username, password);
    }

    @Bean
    public ConnectionFactory syncConnectionFactory(
            @Value("${spring.rabbitmq.sync.host}") String host,
            @Value("${spring.rabbitmq.sync.port}") int port,
            @Value("${spring.rabbitmq.sync.username}") String username,
            @Value("${spring.rabbitmq.sync.password}") String password
    ) {
        return connectionFactory(host, port, username, password);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Each admin declares only the queues that belong to its own broker
    @Bean
    public RabbitAdmin dataRabbitAdmin(@Qualifier("dataConnectionFactory") ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public RabbitAdmin syncRabbitAdmin(@Qualifier("syncConnectionFactory") ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public Queue deviceDataQueue(
            @Value("${rabbitmq.queue.data}") String name,
            @Qualifier("dataRabbitAdmin") RabbitAdmin admin
    ) {
        Queue queue = new Queue(name, true);
        queue.setAdminsThatShouldDeclare(admin);
        return queue;
    }

    @Bean
    public Queue notificationsQueue(
            @Value("${rabbitmq.queue.notifications}") String name,
            @Qualifier("syncRabbitAdmin") RabbitAdmin admin
    ) {
        Queue queue = new Queue(name, true);
        queue.setAdminsThatShouldDeclare(admin);
        return queue;
    }

    @Bean
    public RabbitTemplate syncRabbitTemplate(
            @Qualifier("syncConnectionFactory") ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory dataRabbitListenerContainerFactory(
            @Qualifier("dataConnectionFactory") ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter
    ) {
        return listenerFactory(connectionFactory, jsonMessageConverter);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory syncRabbitListenerContainerFactory(
            @Qualifier("syncConnectionFactory") ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter
    ) {
        return listenerFactory(connectionFactory, jsonMessageConverter);
    }

    private ConnectionFactory connectionFactory(String host, int port, String username, String password) {
        CachingConnectionFactory factory = new CachingConnectionFactory(host, port);
        factory.setUsername(username);
        factory.setPassword(password);
        return factory;
    }

    private SimpleRabbitListenerContainerFactory listenerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setMissingQueuesFatal(false);
        return factory;
    }
}