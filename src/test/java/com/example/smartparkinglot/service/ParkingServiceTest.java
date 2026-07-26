package com.example.smartparkinglot.service;

import com.example.smartparkinglot.model.*;
import com.example.smartparkinglot.repository.ParkingSpotRepository;
import com.example.smartparkinglot.repository.ParkingTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    @Mock
    private ParkingSpotRepository spotRepository;

    @Mock
    private ParkingTicketRepository ticketRepository;

    @Mock
    private FeeCalculationService feeCalculationService;

    @InjectMocks
    private ParkingService parkingService;

    private ParkingSpot testSpot;

    @BeforeEach
    void setUp() {
        testSpot = ParkingSpot.builder()
                .id(1L)
                .spotNumber("SPOT-A01")
                .floor(1)
                .spotSize(SpotSize.MEDIUM)
                .isAvailable(true)
                .build();
    }

    @Nested
    @DisplayName("Check-In Tests")
    class CheckInTests {

        @Test
        @DisplayName("Should successfully check in a car")
        void shouldCheckInCar() {
            // Arrange
            when(ticketRepository.findFirstByLicensePlateAndStatus("MH-12-AB-1234", TicketStatus.ACTIVE))
                    .thenReturn(Optional.empty());
            when(spotRepository.findAvailableSpotsForUpdate(anyList(), any(PageRequest.class)))
                    .thenReturn(List.of(testSpot));
            when(spotRepository.save(any(ParkingSpot.class))).thenReturn(testSpot);
            when(ticketRepository.save(any(ParkingTicket.class))).thenAnswer(invocation -> {
                ParkingTicket saved = invocation.getArgument(0);
                saved.setId(101L);
                return saved;
            });

            // Act
            ParkingTicket ticket = parkingService.checkIn("MH-12-AB-1234", VehicleType.CAR);

            // Assert
            assertThat(ticket).isNotNull();
            assertThat(ticket.getLicensePlate()).isEqualTo("MH-12-AB-1234");
            assertThat(ticket.getVehicleType()).isEqualTo(VehicleType.CAR);
            assertThat(ticket.getStatus()).isEqualTo(TicketStatus.ACTIVE);
            assertThat(ticket.getParkingSpot().getSpotNumber()).isEqualTo("SPOT-A01");

            // Verify spot was marked as unavailable
            ArgumentCaptor<ParkingSpot> spotCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
            verify(spotRepository).save(spotCaptor.capture());
            assertThat(spotCaptor.getValue().isAvailable()).isFalse();
        }

        @Test
        @DisplayName("Should reject check-in if vehicle already checked in")
        void shouldRejectDuplicateCheckIn() {
            // Arrange
            ParkingTicket existingTicket = ParkingTicket.builder()
                    .id(1L)
                    .licensePlate("MH-12-AB-1234")
                    .status(TicketStatus.ACTIVE)
                    .build();
            when(ticketRepository.findFirstByLicensePlateAndStatus("MH-12-AB-1234", TicketStatus.ACTIVE))
                    .thenReturn(Optional.of(existingTicket));

            // Act & Assert
            assertThatThrownBy(() -> parkingService.checkIn("MH-12-AB-1234", VehicleType.CAR))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already checked in");
        }

        @Test
        @DisplayName("Should throw when no compatible spots available")
        void shouldThrowWhenNoSpotsAvailable() {
            // Arrange
            when(ticketRepository.findFirstByLicensePlateAndStatus("MH-12-AB-1234", TicketStatus.ACTIVE))
                    .thenReturn(Optional.empty());
            when(spotRepository.findAvailableSpotsForUpdate(anyList(), any(PageRequest.class)))
                    .thenReturn(Collections.emptyList());

            // Act & Assert
            assertThatThrownBy(() -> parkingService.checkIn("MH-12-AB-1234", VehicleType.BUS))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No compatible parking spots");
        }

        @Test
        @DisplayName("Should query correct spot sizes for motorcycle")
        void shouldQueryCorrectSizesForMotorcycle() {
            // Arrange
            when(ticketRepository.findFirstByLicensePlateAndStatus(anyString(), any()))
                    .thenReturn(Optional.empty());
            when(spotRepository.findAvailableSpotsForUpdate(anyList(), any(PageRequest.class)))
                    .thenReturn(List.of(testSpot));
            when(spotRepository.save(any())).thenReturn(testSpot);
            when(ticketRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            // Act
            parkingService.checkIn("MH-01-XX-0001", VehicleType.MOTORCYCLE);

            // Assert - motorcycle should search SMALL, MEDIUM, LARGE
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<SpotSize>> sizesCaptor = ArgumentCaptor.forClass(List.class);
            verify(spotRepository).findAvailableSpotsForUpdate(sizesCaptor.capture(), any());
            assertThat(sizesCaptor.getValue()).containsExactly(SpotSize.SMALL, SpotSize.MEDIUM, SpotSize.LARGE);
        }

        @Test
        @DisplayName("Should query correct spot sizes for bus (LARGE only)")
        void shouldQueryCorrectSizesForBus() {
            // Arrange
            ParkingSpot largeSpot = ParkingSpot.builder()
                    .id(2L).spotNumber("SPOT-L01").floor(1).spotSize(SpotSize.LARGE).isAvailable(true).build();
            when(ticketRepository.findFirstByLicensePlateAndStatus(anyString(), any()))
                    .thenReturn(Optional.empty());
            when(spotRepository.findAvailableSpotsForUpdate(anyList(), any(PageRequest.class)))
                    .thenReturn(List.of(largeSpot));
            when(spotRepository.save(any())).thenReturn(largeSpot);
            when(ticketRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            // Act
            parkingService.checkIn("MH-01-BUS-0001", VehicleType.BUS);

            // Assert - bus should search LARGE only
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<SpotSize>> sizesCaptor = ArgumentCaptor.forClass(List.class);
            verify(spotRepository).findAvailableSpotsForUpdate(sizesCaptor.capture(), any());
            assertThat(sizesCaptor.getValue()).containsExactly(SpotSize.LARGE);
        }
    }

    @Nested
    @DisplayName("Check-Out Tests")
    class CheckOutTests {

        @Test
        @DisplayName("Should successfully check out and calculate fee")
        void shouldCheckOutAndCalculateFee() {
            // Arrange
            ParkingTicket activeTicket = ParkingTicket.builder()
                    .id(101L)
                    .licensePlate("MH-12-AB-1234")
                    .vehicleType(VehicleType.CAR)
                    .parkingSpot(testSpot)
                    .entryTime(LocalDateTime.of(2026, 7, 26, 10, 0))
                    .status(TicketStatus.ACTIVE)
                    .build();

            when(ticketRepository.findFirstByLicensePlateAndStatus("MH-12-AB-1234", TicketStatus.ACTIVE))
                    .thenReturn(Optional.of(activeTicket));
            when(feeCalculationService.calculateFee(any(), any(), eq(VehicleType.CAR)))
                    .thenReturn(60.0);
            when(spotRepository.save(any(ParkingSpot.class))).thenReturn(testSpot);
            when(ticketRepository.save(any(ParkingTicket.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            ParkingTicket result = parkingService.checkOut("MH-12-AB-1234");

            // Assert
            assertThat(result.getStatus()).isEqualTo(TicketStatus.COMPLETED);
            assertThat(result.getFee()).isEqualTo(60.0);
            assertThat(result.getExitTime()).isNotNull();

            // Verify spot was released
            ArgumentCaptor<ParkingSpot> spotCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
            verify(spotRepository).save(spotCaptor.capture());
            assertThat(spotCaptor.getValue().isAvailable()).isTrue();
        }

        @Test
        @DisplayName("Should throw when no active ticket found for checkout")
        void shouldThrowWhenNoActiveTicket() {
            // Arrange
            when(ticketRepository.findFirstByLicensePlateAndStatus("UNKNOWN-PLATE", TicketStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> parkingService.checkOut("UNKNOWN-PLATE"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("No active ticket found");
        }
    }

    @Nested
    @DisplayName("Spot Creation Tests")
    class SpotCreationTests {

        @Test
        @DisplayName("Should create a new parking spot with availability set to true")
        void shouldCreateSpotAsAvailable() {
            // Arrange
            when(spotRepository.save(any(ParkingSpot.class))).thenAnswer(invocation -> {
                ParkingSpot spot = invocation.getArgument(0);
                spot.setId(10L);
                return spot;
            });

            // Act
            ParkingSpot created = parkingService.createSpot("SPOT-NEW-01", 2, SpotSize.LARGE);

            // Assert
            assertThat(created.getSpotNumber()).isEqualTo("SPOT-NEW-01");
            assertThat(created.getFloor()).isEqualTo(2);
            assertThat(created.getSpotSize()).isEqualTo(SpotSize.LARGE);
            assertThat(created.isAvailable()).isTrue();
        }
    }
}
