package com.example.smartparkinglot.repository;

import com.example.smartparkinglot.model.ParkingSpot;
import com.example.smartparkinglot.model.SpotSize;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ParkingSpot s WHERE s.isAvailable = true AND s.spotSize IN :sizes " +
           "ORDER BY CASE s.spotSize WHEN 'SMALL' THEN 0 WHEN 'MEDIUM' THEN 1 WHEN 'LARGE' THEN 2 END ASC, s.id ASC")
    List<ParkingSpot> findAvailableSpotsForUpdate(@Param("sizes") List<SpotSize> sizes, Pageable pageable);

    long countByIsAvailableTrue();

    long countBySpotSizeAndIsAvailableTrue(SpotSize spotSize);

    long countBySpotSize(SpotSize spotSize);
}
