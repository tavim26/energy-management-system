package com.energymanagement.simulator;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

// Command-line application that sends power measurements for one device at a fixed interval
public class DeviceSimulator {

    private static final String DEFAULT_CONFIG = "config.properties";

    // In fast-forward mode, a measurement is sent every 5 seconds
    // and each one is timestamped 10 minutes after the previous one
    private static final long FAST_FORWARD_PERIOD_SECONDS = 5;
    private static final long SIMULATED_STEP_MINUTES = 10;

    private enum Mode { FAST_FORWARD, NORMAL }

    private final SimulatorConfig config;
    private final RabbitMQProducer producer;
    private final MeasurementGenerator generator;

    private DeviceSimulator(SimulatorConfig config, RabbitMQProducer producer, MeasurementGenerator generator) {
        this.config = config;
        this.producer = producer;
        this.generator = generator;
    }

    public static void main(String[] args) {
        String configPath = args.length > 0 ? args[0] : DEFAULT_CONFIG;

        try {
            SimulatorConfig config = SimulatorConfig.load(configPath);
            RabbitMQProducer producer = new RabbitMQProducer(config);

            System.out.printf("Device %d, base load %.0f W%n", config.deviceId(), config.baseLoadWatts());

            Mode mode = askForMode(config.intervalMinutes());
            new DeviceSimulator(config, producer, new MeasurementGenerator(config.baseLoadWatts())).run(mode);

        } catch (Exception e) {
            System.err.println("Simulator stopped: " + e.getMessage());
            System.exit(1);
        }
    }

    private void run(Mode mode) throws InterruptedException {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        LocalDateTime start = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        AtomicLong sentCount = new AtomicLong();

        Runnable sendMeasurement = () -> {
            LocalDateTime timestamp = mode == Mode.FAST_FORWARD
                    ? start.plusMinutes(SIMULATED_STEP_MINUTES * sentCount.get())
                    : LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            DeviceMessage message = new DeviceMessage(timestamp, config.deviceId(), generator.powerAt(timestamp));

            // An exception escaping this task would silently stop all the following runs
            try {
                producer.send(message);
                sentCount.incrementAndGet();
                System.out.printf("Sent %s  %8.2f W%n", timestamp, message.measurementValue());
            } catch (Exception e) {
                System.err.println("Failed to send measurement: " + e.getMessage());
            }
        };

        if (mode == Mode.FAST_FORWARD) {
            scheduler.scheduleAtFixedRate(sendMeasurement, 0, FAST_FORWARD_PERIOD_SECONDS, TimeUnit.SECONDS);
        } else {
            scheduler.scheduleAtFixedRate(sendMeasurement, 0, config.intervalMinutes(), TimeUnit.MINUTES);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.shutdownNow();

            try {
                producer.close();
            } catch (Exception e) {
                System.err.println("Could not close the RabbitMQ connection: " + e.getMessage());
            }

            System.out.println("Simulator stopped after " + sentCount.get() + " measurements");
        }));

        System.out.println("Simulator running. Press Ctrl+C to stop.");

        // Keeps the application alive; the scheduler sends the measurements in the background
        Thread.currentThread().join();
    }

    private static Mode askForMode(int intervalMinutes) {
        Scanner scanner = new Scanner(System.in);

        System.out.println();
        System.out.println("Choose a simulation mode:");
        System.out.printf("  [1] Fast-forward: a measurement every %d seconds, each one %d simulated minutes later%n",
                FAST_FORWARD_PERIOD_SECONDS, SIMULATED_STEP_MINUTES);
        System.out.printf("  [2] Normal: a measurement every %d minutes, in real time%n", intervalMinutes);

        while (true) {
            System.out.print("Mode (1/2): ");

            if (!scanner.hasNextLine()) {
                throw new IllegalStateException("No simulation mode was chosen");
            }

            String input = scanner.nextLine().trim();

            if (input.equals("1")) {
                return Mode.FAST_FORWARD;
            }

            if (input.equals("2")) {
                return Mode.NORMAL;
            }

            System.out.println("Please type 1 or 2.");
        }
    }
}