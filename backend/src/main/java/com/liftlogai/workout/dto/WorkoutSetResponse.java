package com.liftlogai.workout.dto;

import java.math.BigDecimal;

public record WorkoutSetResponse(
        Long id,
        int setNumber,
        Integer reps,
        BigDecimal weight,
        Integer durationSeconds,
        BigDecimal distance,
        boolean completed,
        String notes
) {
}
