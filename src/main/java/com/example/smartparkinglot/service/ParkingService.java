package com.example.smartparkinglot.service;

import com.example.smartparkinglot.dto.AvailabilityResponse;
import com.example.smartparkinglot.model.*;
import com.example.smartparkinglot.repository.ParkingSpotRepository;
import com.example.smartparkinglot.repository.ParkingTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ParkingService {

    private final ParkingSpotRepository spotRepository;
    private final ParkingTicketRepository ticketRepository;
    private final FeeCalculationService feeCalculationService;

    @Transactional
    public ParkingTicket checkIn(String licensePlate, VehicleType vehicleType) {
        // 1. Ensure no current active tickets for this vehicle
        ticketRepository.findFirstByLicensePlateAndStatus(licensePlate, TicketStatus.ACTIVE)
                .ifPresent(t -> {
                    throw new IllegalStateException("Vehicle with license plate '" + licensePlate + "' is already checked in.");
                });

        // 2. Fetch compatible spot sizes based on vehicle size hierarchy
        List<SpotSize> compatibleSizes = vehicleType.getCompatibleSpotSizes();

        // 3. Find and lock first available spot (pessimistic write lock for concurrency)
        List<ParkingSpot> availableSpots = spotRepository.findAvailableSpotsForUpdate(
                compatibleSizes, PageRequest.of(0, 1));

        if (availableSpots.isEmpty()) {
            throw new IllegalStateException("No compatible parking spots available for vehicle type: " + vehicleType);
        }

        ParkingSpot spot = availableSpots.get(0);
        spot.setAvailable(false);
        spotRepository.save(spot);

        // 4. Create and save parking ticket
        ParkingTicket ticket = ParkingTicket.builder()
                .licensePlate(licensePlate)
                .vehicleType(vehicleType)
                .parkingSpot(spot)
                .entryTime(LocalDateTime.now())
                .status(TicketStatus.ACTIVE)
                .build();

        return ticketRepository.save(ticket);
    }

    @Transactional
    public ParkingTicket checkOut(String licensePlate) {
        // 1. Find active ticket
        ParkingTicket ticket = ticketRepository.findFirstByLicensePlateAndStatus(licensePlate, TicketStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No active ticket found for license plate: " + licensePlate));

        // 2. Calculate fee
        LocalDateTime exitTime = LocalDateTime.now();
        double fee = feeCalculationService.calculateFee(ticket.getEntryTime(), exitTime, ticket.getVehicleType());

        // 3. Release parking spot
        ParkingSpot spot = ticket.getParkingSpot();
        spot.setAvailable(true);
        spotRepository.save(spot);

        // 4. Complete ticket
        ticket.setExitTime(exitTime);
        ticket.setFee(fee);
        ticket.setStatus(TicketStatus.COMPLETED);

        return ticketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse getAvailability() {
        long totalSpots = spotRepository.count();
        long availableSpots = spotRepository.countByIsAvailableTrue();

        Map<String, AvailabilityResponse.SpotAvailability> breakdown = new LinkedHashMap<>();
        for (SpotSize size : SpotSize.values()) {
            long total = spotRepository.countBySpotSize(size);
            long available = spotRepository.countBySpotSizeAndIsAvailableTrue(size);
            breakdown.put(size.name(), AvailabilityResponse.SpotAvailability.builder()
                    .total(total)
                    .available(available)
                    .build());
        }

        return AvailabilityResponse.builder()
                .totalSpots(totalSpots)
                .availableSpots(availableSpots)
                .breakdown(breakdown)
                .build();
    }

    @Transactional
    public ParkingSpot createSpot(String spotNumber, Integer floor, SpotSize spotSize) {
        ParkingSpot spot = ParkingSpot.builder()
                .spotNumber(spotNumber)
                .floor(floor)
                .spotSize(spotSize)
                .isAvailable(true)
                .build();
        return spotRepository.save(spot);
    }

    @Transactional(readOnly = true)
    public List<ParkingSpot> getAllSpots() {
        return spotRepository.findAll();
    }
}
