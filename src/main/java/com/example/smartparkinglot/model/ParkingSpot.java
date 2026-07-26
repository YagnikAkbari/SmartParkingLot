package com.example.smartparkinglot.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parking_spots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "spot_number", unique = true, nullable = false)
    private String spotNumber;

    @Column(name = "floor", nullable = false)
    private Integer floor;

    @Enumerated(EnumType.STRING)
    @Column(name = "spot_size", nullable = false)
    private SpotSize spotSize;

    @Column(name = "is_available", nullable = false)
    private boolean isAvailable;
}
