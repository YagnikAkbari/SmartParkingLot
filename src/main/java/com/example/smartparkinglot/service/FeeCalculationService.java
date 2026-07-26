package com.example.smartparkinglot.service;

import com.example.smartparkinglot.model.VehicleType;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class FeeCalculationService {

    public double calculateFee(LocalDateTime entry, LocalDateTime exit, VehicleType type) {
        long durationMinutes = Duration.between(entry, exit).toMinutes();

        // Round up to nearest hour (minimum 1 hour charge)
        long durationHours = (long) Math.ceil((double) durationMinutes / 60.0);
        if (durationHours <= 0) {
            durationHours = 1;
        }

        double ratePerHour = getRateForVehicleType(type);
        return durationHours * ratePerHour;
    }

    private double getRateForVehicleType(VehicleType type) {
        return switch (type) {
            case MOTORCYCLE -> 10.0;
            case CAR -> 20.0;
            case BUS -> 50.0;
        };
    }
}
