package com.example.smartparkinglot.controller;

import com.example.smartparkinglot.dto.SpotCreationRequest;
import com.example.smartparkinglot.model.ParkingSpot;
import com.example.smartparkinglot.service.ParkingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ParkingService parkingService;

    @PostMapping("/spots")
    public ResponseEntity<ParkingSpot> createSpot(@Valid @RequestBody SpotCreationRequest request) {
        ParkingSpot spot = parkingService.createSpot(
                request.getSpotNumber(), request.getFloor(), request.getSpotSize());
        return ResponseEntity.status(HttpStatus.CREATED).body(spot);
    }

    @GetMapping("/spots")
    public ResponseEntity<List<ParkingSpot>> getAllSpots() {
        return ResponseEntity.ok(parkingService.getAllSpots());
    }
}
