package com.liftlogai.exercise.dto;

import java.math.BigDecimal;

public record ExerciseProgressionResponse(
        ExerciseSummaryResponse exercise,
        long workoutCount,
        long completedSetCount,
        Integer bestReps,
        BigDecimal bestWeight,
        BigDecimal totalVolume
) {
}
