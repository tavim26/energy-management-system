package com.energymanagement.simulator;

import java.time.LocalDateTime;
import java.util.Random;

// Produces realistic power readings: low at night, highest in the evening, with small random variations
public class MeasurementGenerator {

    // Each reading varies by up to 5% in either direction
    private static final double MAX_FLUCTUATION = 0.05;

    private final double baseLoadWatts;
    private final Random random = new Random();

    public MeasurementGenerator(double baseLoadWatts) {
        this.baseLoadWatts = baseLoadWatts;
    }

    // Average power, in watts, rounded to two decimals
    public double powerAt(LocalDateTime time) {
        double fluctuation = 1 + (random.nextDouble() * 2 - 1) * MAX_FLUCTUATION;
        double watts = baseLoadWatts * dailyFactor(time.getHour()) * fluctuation;

        return Math.round(watts * 100) / 100.0;
    }

    // Share of the base load used at each time of day
    private double dailyFactor(int hour) {
        if (hour < 6) {
            return between(0.5, 0.7); // night
        }
        if (hour < 9) {
            return between(0.7, 0.9); // morning
        }
        if (hour < 17) {
            return between(0.6, 0.8); // working hours
        }
        if (hour < 23) {
            return between(0.8, 1.1); // evening peak
        }
        return between(0.6, 0.8); // late evening
    }

    private double between(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }
}