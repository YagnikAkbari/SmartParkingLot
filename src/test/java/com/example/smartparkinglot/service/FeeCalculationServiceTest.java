package com.example.smartparkinglot.service;

import com.example.smartparkinglot.model.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class FeeCalculationServiceTest {

    private FeeCalculationService feeCalculationService;

    @BeforeEach
    void setUp() {
        feeCalculationService = new FeeCalculationService();
    }

    @Nested
    @DisplayName("Motorcycle Fee Tests")
    class MotorcycleFeeTests {

        @Test
        @DisplayName("Should charge $10 for exactly 1 hour")
        void shouldChargeForOneHour() {
            LocalDateTime entry = LocalDateTime.of(2026, 7, 26, 10, 0);
            LocalDateTime exit = LocalDateTime.of(2026, 7, 26, 11, 0);

            double fee = feeCalculationService.calculateFee(entry, exit, VehicleType.MOTORCYCLE);

            assertThat(fee).isEqualTo(10.0);
        }

        @Test
        @DisplayName("Should round up partial hour - 1h30m charged as 2 hours = $20")
        void shouldRoundUpPartialHour() {
            LocalDateTime entry = LocalDateTime.of(2026, 7, 26, 10, 0);
            LocalDateTime exit = LocalDateTime.of(2026, 7, 26, 11, 30);

            double fee = feeCalculationService.calculateFee(entry, exit, VehicleType.MOTORCYCLE);

            assertThat(fee).isEqualTo(20.0);
        }

        @Test
        @DisplayName("Should charge minimum 1 hour for very short stays")
        void shouldChargeMinimumOneHour() {
            LocalDateTime entry = LocalDateTime.of(2026, 7, 26, 10, 0);
            LocalDateTime exit = LocalDateTime.of(2026, 7, 26, 10, 5);

            double fee = feeCalculationService.calculateFee(entry, exit, VehicleType.MOTORCYCLE);

            assertThat(fee).isEqualTo(10.0);
        }
    }

    @Nested
    @DisplayName("Car Fee Tests")
    class CarFeeTests {

        @Test
        @DisplayName("Should charge $20 for exactly 1 hour")
        void shouldChargeForOneHour() {
            LocalDateTime entry = LocalDateTime.of(2026, 7, 26, 10, 0);
            LocalDateTime exit = LocalDateTime.of(2026, 7, 26, 11, 0);

            double fee = feeCalculationService.calculateFee(entry, exit, VehicleType.CAR);

            assertThat(fee).isEqualTo(20.0);
        }

        @Test
        @DisplayName("Should charge $60 for exactly 3 hours")
        void shouldChargeForThreeHours() {
            LocalDateTime entry = LocalDateTime.of(2026, 7, 26, 10, 0);
            LocalDateTime exit = LocalDateTime.of(2026, 7, 26, 13, 0);

            double fee = feeCalculationService.calculateFee(entry, exit, VehicleType.CAR);

            assertThat(fee).isEqualTo(60.0);
        }

        @Test
        @DisplayName("Should round up partial hours - 2h15m charged as 3 hours = $60")
        void shouldRoundUpPartialHour() {
            LocalDateTime entry = LocalDateTime.of(2026, 7, 26, 10, 0);
            LocalDateTime exit = LocalDateTime.of(2026, 7, 26, 12, 15);

            double fee = feeCalculationService.calculateFee(entry, exit, VehicleType.CAR);

            assertThat(fee).isEqualTo(60.0);
        }
    }

    @Nested
    @DisplayName("Bus Fee Tests")
    class BusFeeTests {

        @Test
        @DisplayName("Should charge $50 for exactly 1 hour")
        void shouldChargeForOneHour() {
            LocalDateTime entry = LocalDateTime.of(2026, 7, 26, 10, 0);
            LocalDateTime exit = LocalDateTime.of(2026, 7, 26, 11, 0);

            double fee = feeCalculationService.calculateFee(entry, exit, VehicleType.BUS);

            assertThat(fee).isEqualTo(50.0);
        }

        @Test
        @DisplayName("Should charge $250 for 5 hours")
        void shouldChargeForFiveHours() {
            LocalDateTime entry = LocalDateTime.of(2026, 7, 26, 8, 0);
            LocalDateTime exit = LocalDateTime.of(2026, 7, 26, 13, 0);

            double fee = feeCalculationService.calculateFee(entry, exit, VehicleType.BUS);

            assertThat(fee).isEqualTo(250.0);
        }
    }

    @Test
    @DisplayName("Should charge minimum 1 hour even for instant exit")
    void shouldChargeMinimumOneHourForInstantExit() {
        LocalDateTime entry = LocalDateTime.of(2026, 7, 26, 10, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 7, 26, 10, 0);

        double fee = feeCalculationService.calculateFee(entry, exit, VehicleType.CAR);

        assertThat(fee).isEqualTo(20.0);
    }
}
