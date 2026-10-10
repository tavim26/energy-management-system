package com.energymanagement.simulator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.function.Function;

// Settings read from a .properties file (see src/main/resources/config.properties)
public record SimulatorConfig(
        long deviceId,
        String rabbitHost,
        int rabbitPort,
        String rabbitUsername,
        String rabbitPassword,
        String queueName,
        int intervalMinutes,
        double baseLoadWatts
) {

    public static SimulatorConfig load(String path) throws IOException {
        Properties properties = new Properties();

        try (InputStream input = open(path)) {
            properties.load(input);
        }

        return new SimulatorConfig(
                parse(properties, "device.id", null, Long::parseLong),
                value(properties, "rabbitmq.host", "localhost"),
                parse(properties, "rabbitmq.port", "5672", Integer::parseInt),
                value(properties, "rabbitmq.username", "guest"),
                value(properties, "rabbitmq.password", "guest"),
                value(properties, "rabbitmq.queue.name", "device-data-queue"),
                parse(properties, "simulator.interval.minutes", "10", Integer::parseInt),
                parse(properties, "simulator.base.load", "1000", Double::parseDouble)
        );
    }

    // The file is looked up on disk first, then inside the jar
    private static InputStream open(String path) throws IOException {
        Path file = Path.of(path);

        if (Files.isRegularFile(file)) {
            System.out.println("Configuration: " + file.toAbsolutePath());
            return Files.newInputStream(file);
        }

        InputStream resource = SimulatorConfig.class.getClassLoader().getResourceAsStream(path);

        if (resource == null) {
            throw new FileNotFoundException("Configuration file not found: " + path);
        }

        System.out.println("Configuration: " + path + " (packaged in the jar)");
        return resource;
    }

    private static String value(Properties properties, String key, String defaultValue) {
        String value = properties.getProperty(key, defaultValue);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing setting: " + key);
        }

        return value.trim();
    }

    private static <T> T parse(Properties properties, String key, String defaultValue, Function<String, T> parser) {
        String value = value(properties, key, defaultValue);

        try {
            return parser.apply(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid value for " + key + ": " + value);
        }
    }
}