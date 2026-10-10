package com.energymanagement.simulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

// Publishes measurements to the data broker queue read by the Monitoring Service
public class RabbitMQProducer implements AutoCloseable {

    private static final AMQP.BasicProperties MESSAGE_PROPERTIES = new AMQP.BasicProperties.Builder()
            .contentType("application/json")
            .contentEncoding("UTF-8")
            // Persistent: the message survives a broker restart while it waits in the queue
            .deliveryMode(2)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final Connection connection;
    private final Channel channel;
    private final String queueName;

    public RabbitMQProducer(SimulatorConfig config) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(config.rabbitHost());
        factory.setPort(config.rabbitPort());
        factory.setUsername(config.rabbitUsername());
        factory.setPassword(config.rabbitPassword());

        this.connection = factory.newConnection("device-simulator");
        this.channel = connection.createChannel();
        this.queueName = config.queueName();

        // Same settings as in the Monitoring Service (durable, not exclusive, not auto-deleted),
        // so the queue exists even if the simulator starts first
        channel.queueDeclare(queueName, true, false, false, null);

        System.out.printf("Connected to RabbitMQ at %s:%d (queue %s)%n",
                config.rabbitHost(), config.rabbitPort(), queueName);
    }

    public void send(DeviceMessage message) throws IOException {
        byte[] body = objectMapper.writeValueAsBytes(message);
        // The default exchange ("") routes the message to the queue with the same name as the routing key
        channel.basicPublish("", queueName, MESSAGE_PROPERTIES, body);
    }

    @Override
    public void close() throws IOException, TimeoutException {
        if (channel.isOpen()) {
            channel.close();
        }

        if (connection.isOpen()) {
            connection.close();
        }
    }
}