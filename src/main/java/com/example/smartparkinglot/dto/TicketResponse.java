package com.example.smartparkinglot.dto;

import com.example.smartparkinglot.model.ParkingTicket;
import com.example.smartparkinglot.model.TicketStatus;
import com.example.smartparkinglot.model.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {

    private Long ticketId;
    private String licensePlate;
    private VehicleType vehicleType;
    private String spotNumber;
    private Integer floor;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Double fee;
    private TicketStatus status;

    public static TicketResponse fromEntity(ParkingTicket ticket) {
        return TicketResponse.builder()
                .ticketId(ticket.getId())
                .licensePlate(ticket.getLicensePlate())
                .vehicleType(ticket.getVehicleType())
                .spotNumber(ticket.getParkingSpot().getSpotNumber())
                .floor(ticket.getParkingSpot().getFloor())
                .entryTime(ticket.getEntryTime())
                .exitTime(ticket.getExitTime())
                .fee(ticket.getFee())
                .status(ticket.getStatus())
                .build();
    }
}
