package com.energymanagement.simulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

// Producer RabbitMQ - stabileste conexiunea si trimite mesaje
public class RabbitMQProducer
{

    private Connection connection;
    private Channel channel;
    private String queueName;
    private ObjectMapper objectMapper;

    // Constructor - stabileste conexiunea cu RabbitMQ
    public RabbitMQProducer(String host, int port, String username, String password, String queueName) throws IOException, TimeoutException
    {
        this.queueName = queueName;

        // Configureaza ObjectMapper pentru serializare JSON
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        // Configureaza factory pentru conexiune RabbitMQ
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);

        // Creeaza conexiunea si channel-ul
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();

        System.out.println("Connected to RabbitMQ at " + host + ":" + port);
        System.out.println("Queue: " + queueName);
    }

    // Trimite mesaj in queue
    public void sendMessage(DeviceMessage message) throws IOException
    {
        // Serializeaza obiectul ca JSON string
        String jsonMessage = objectMapper.writeValueAsString(message);

        // Publica mesajul in queue
        // Parametri: (exchange, routingKey, props, body)
        // exchange="" -> foloseste default exchange (direct)
        // routingKey= queueName -> mesajul merge direct in queue
        channel.basicPublish("", queueName, null, jsonMessage.getBytes());

        System.out.println("Sent: " + message);
    }



    // Inchide conexiunea cu RabbitMQ
    public void close() throws IOException, TimeoutException
    {
        if (channel != null && channel.isOpen())
        {
            channel.close();
        }

        if (connection != null && connection.isOpen())
        {
            connection.close();
        }

        System.out.println("RabbitMQ connection closed.");
    }
}