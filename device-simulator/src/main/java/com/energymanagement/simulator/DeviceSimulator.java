package com.energymanagement.simulator;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class DeviceSimulator
{

    private ConfigLoader config;
    private RabbitMQProducer producer;
    private Random random;
    private double currentLoad;


    public DeviceSimulator(String configFile) throws Exception
    {
        this.config = new ConfigLoader(configFile);
        this.random = new Random();
        this.currentLoad = config.getBaseLoad();

        this.producer = new RabbitMQProducer(
                config.getRabbitMQHost(),
                config.getRabbitMQPort(),
                config.getRabbitMQUsername(),
                config.getRabbitMQPassword(),
                config.getQueueName()
        );

        System.out.println("Device Simulator Started");
        System.out.println("Device ID: " + config.getDeviceId());
        System.out.println("Base Load: " + config.getBaseLoad() + " W");
        System.out.println("Interval: " + config.getIntervalMinutes() + " minutes");
    }


    private double generateMeasurement(LocalDateTime timestamp)
    {
        int hour = timestamp.getHour();

        double timeFactor = 1.0;

        if (hour >= 0 && hour < 6)
        {
            timeFactor = 0.5 + random.nextDouble() * 0.2;  // 50-70%

        }
        else if (hour >= 6 && hour < 9)
        {
            timeFactor = 0.7 + random.nextDouble() * 0.2;  // 70-90%

        }
        else if (hour >= 9 && hour < 17)
        {
            timeFactor = 0.6 + random.nextDouble() * 0.2;  // 60-80%

        }
        else if (hour >= 17 && hour < 23)
        {
            timeFactor = 0.8 + random.nextDouble() * 0.3;  // 80-110%

        }
        else
        {
            timeFactor = 0.6 + random.nextDouble() * 0.2;  // 60-80%
        }

        double fluctuation = (random.nextDouble() - 0.5) * 0.1;

        currentLoad = config.getBaseLoad() * timeFactor * (1 + fluctuation);

        return Math.max(0, currentLoad);
    }




    public void start(boolean fastForwardMode)
    {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        final LocalDateTime[] simulatedTime = {LocalDateTime.now()};

        long interval;
        TimeUnit timeUnit;

        if (fastForwardMode)
        {
            interval = 5;
            timeUnit = TimeUnit.SECONDS;

            System.out.println("FAST-FORWARD MODE activated");
            System.out.println("Starting from: " + simulatedTime[0]);
            System.out.println("Sending measurements every 5 SECONDS with accelerated timestamps\n");
        }
        else
        {
            interval = config.getIntervalMinutes();
            timeUnit = TimeUnit.MINUTES;

            System.out.println("NORMAL MODE activated");
            System.out.println("Starting from: " + simulatedTime[0]);
            System.out.println("Sending measurements every " + interval + " MINUTES with real timestamps\n");
        }

        scheduler.scheduleAtFixedRate(() -> {
            try {
                LocalDateTime timestamp;

                if (fastForwardMode)
                {
                    timestamp = simulatedTime[0];

                    simulatedTime[0] = simulatedTime[0].plusMinutes(10);
                }
                else
                {
                    timestamp = LocalDateTime.now();
                }

                double measurement = generateMeasurement(timestamp);

                DeviceMessage message = new DeviceMessage(
                        timestamp,
                        config.getDeviceId(),
                        measurement
                );

                producer.sendMessage(message);

            } catch (Exception e)
            {
                System.err.println("Error sending message: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, interval, timeUnit);


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down simulator");

            scheduler.shutdown();

            try {
                producer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        System.out.println("Simulator is running.\n");
    }




    public static void main(String[] args)
    {
        // Configurare default
        String configFile = "config.properties";

        if (args.length > 0)
        {
            configFile = args[0];
        }

        try {
            // Creeaza simulator-ul
            DeviceSimulator simulator = new DeviceSimulator(configFile);

            // Intreaba utilizatorul despre modul de simulare
            boolean fastForwardMode = askForSimulationMode();

            // Porneste cu modul ales
            simulator.start(fastForwardMode);

            // Pastreaza main thread activ
            Thread.currentThread().join();

        } catch (Exception e)
        {
            System.err.println("Failed to start simulator: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }




    private static boolean askForSimulationMode()
    {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose simulation mode:");
        System.out.println("[1] FAST-FORWARD MODE");
        System.out.println("- Sends data every 2 seconds ");
        System.out.println("- Each message advances 10 minutes");
        System.out.println("- For quick demonstration of hourly aggregation and overconsumption alerts");  // <-- MODIFICA
        System.out.println();

        System.out.println("[2] NORMAL MODE");
        System.out.println("- Sends data every 10 minutes");
        System.out.println("- Standard operation as per assignment requirements");
        System.out.println();

        System.out.print("Enter your choice (1/2): ");

        String input = scanner.nextLine().trim();
        boolean fastForwardMode = input.equals("1");

        System.out.println();

        return fastForwardMode;
    }
}