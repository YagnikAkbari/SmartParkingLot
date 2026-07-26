package com.example.smartparkinglot.repository;

import com.example.smartparkinglot.model.ParkingTicket;
import com.example.smartparkinglot.model.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParkingTicketRepository extends JpaRepository<ParkingTicket, Long> {

    Optional<ParkingTicket> findFirstByLicensePlateAndStatus(String licensePlate, TicketStatus status);
}
