package com.example.smartparkinglot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityResponse {

    private long totalSpots;
    private long availableSpots;
    private Map<String, SpotAvailability> breakdown;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SpotAvailability {
        private long total;
        private long available;
    }
}
