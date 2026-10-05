package com.energymanagement.simulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQProducer
{

    private Connection connection;
    private Channel channel;
    private String queueName;
    private ObjectMapper objectMapper;

    public RabbitMQProducer(String host, int port, String username, String password, String queueName) throws IOException, TimeoutException
    {
        this.queueName = queueName;

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);

        this.connection = factory.newConnection();
        this.channel = connection.createChannel();

        System.out.println("Connected to RabbitMQ at " + host + ":" + port);
        System.out.println("Queue: " + queueName);
    }

    public void sendMessage(DeviceMessage message) throws IOException
    {
        String jsonMessage = objectMapper.writeValueAsString(message);

        channel.basicPublish("", queueName, null, jsonMessage.getBytes());

        System.out.println("Sent: " + message);
    }



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