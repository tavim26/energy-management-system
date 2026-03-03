package com.energymanagement.simulator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

 // Incarca configuratia simulator-ului din fisier .properties
public class ConfigLoader
 {

    private Properties properties;

    // Constructor - incarca configuratia din fisier
    public ConfigLoader(String configFilePath) throws IOException
    {
        properties = new Properties();

        // Incarca din file system (ex: /home/user/config.properties)
        try (FileInputStream fis = new FileInputStream(configFilePath))
        {
            properties.load(fis);
            System.out.println("Configuration loaded from: " + configFilePath);

        } catch (IOException e)
        {
            // Daca nu gaseste, incarca din classpath (src/main/resources)
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(configFilePath))
            {
                if (is != null)
                {
                    properties.load(is);
                    System.out.println("Configuration loaded from classpath: " + configFilePath);
                }
                else
                {
                    throw new IOException("Configuration file not found: " + configFilePath);
                }
            }
        }
    }



    public Long getDeviceId()
    {
        return Long.parseLong(properties.getProperty("device.id"));
    }


    public String getRabbitMQHost()
    {
        return properties.getProperty("rabbitmq.host");
    }


    public int getRabbitMQPort()
    {
        return Integer.parseInt(properties.getProperty("rabbitmq.port", "5672"));
    }


    public String getRabbitMQUsername()
    {
        return properties.getProperty("rabbitmq.username", "guest");
    }



    public String getRabbitMQPassword()
    {
        return properties.getProperty("rabbitmq.password", "guest");
    }


    public String getQueueName()
    {
        return properties.getProperty("rabbitmq.queue.name");
    }


    public int getIntervalMinutes()
    {
        return Integer.parseInt(properties.getProperty("simulator.interval.minutes", "10"));
    }


    public double getBaseLoad()
    {
        return Double.parseDouble(properties.getProperty("simulator.base.load", "1000.0"));
    }
}