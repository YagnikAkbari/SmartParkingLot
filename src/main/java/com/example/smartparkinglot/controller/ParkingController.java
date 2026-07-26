package com.example.smartparkinglot.controller;

import com.example.smartparkinglot.dto.AvailabilityResponse;
import com.example.smartparkinglot.dto.CheckInRequest;
import com.example.smartparkinglot.dto.CheckOutRequest;
import com.example.smartparkinglot.dto.TicketResponse;
import com.example.smartparkinglot.model.ParkingTicket;
import com.example.smartparkinglot.service.ParkingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;

    @PostMapping("/check-in")
    public ResponseEntity<TicketResponse> checkIn(@Valid @RequestBody CheckInRequest request) {
        ParkingTicket ticket = parkingService.checkIn(request.getLicensePlate(), request.getVehicleType());
        return ResponseEntity.ok(TicketResponse.fromEntity(ticket));
    }

    @PostMapping("/check-out")
    public ResponseEntity<TicketResponse> checkOut(@Valid @RequestBody CheckOutRequest request) {
        ParkingTicket ticket = parkingService.checkOut(request.getLicensePlate());
        return ResponseEntity.ok(TicketResponse.fromEntity(ticket));
    }

    @GetMapping("/availability")
    public ResponseEntity<AvailabilityResponse> getAvailability() {
        return ResponseEntity.ok(parkingService.getAvailability());
    }
}
