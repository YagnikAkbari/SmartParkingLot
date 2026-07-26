package com.example.smartparkinglot.model;

import java.util.Arrays;
import java.util.List;

public enum VehicleType {
    MOTORCYCLE {
        @Override
        public List<SpotSize> getCompatibleSpotSizes() {
            return Arrays.asList(SpotSize.SMALL, SpotSize.MEDIUM, SpotSize.LARGE);
        }
    },
    CAR {
        @Override
        public List<SpotSize> getCompatibleSpotSizes() {
            return Arrays.asList(SpotSize.MEDIUM, SpotSize.LARGE);
        }
    },
    BUS {
        @Override
        public List<SpotSize> getCompatibleSpotSizes() {
            return List.of(SpotSize.LARGE);
        }
    };

    public abstract List<SpotSize> getCompatibleSpotSizes();
}
