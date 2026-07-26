package com.example.smartparkinglot.dto;

import com.example.smartparkinglot.model.SpotSize;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpotCreationRequest {

    @NotBlank(message = "Spot number is required")
    private String spotNumber;

    @NotNull(message = "Floor is required")
    @Min(value = 1, message = "Floor must be at least 1")
    private Integer floor;

    @NotNull(message = "Spot size is required")
    private SpotSize spotSize;
}
