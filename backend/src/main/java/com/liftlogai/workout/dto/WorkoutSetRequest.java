package com.liftlogai.workout.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record WorkoutSetRequest(
        @NotNull @Min(1) Integer setNumber,
        @Min(0) Integer reps,
        @DecimalMin("0.0") BigDecimal weight,
        @Min(0) Integer durationSeconds,
        @DecimalMin("0.0") BigDecimal distance,
        Boolean completed,
        @Size(max = 1000) String notes
) {
}
