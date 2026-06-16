package com.liftlogai.workout.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record WorkoutResponse(
        Long id,
        LocalDate workoutDate,
        String title,
        String notes,
        List<WorkoutExerciseResponse> exercises,
        Instant createdAt,
        Instant updatedAt
) {
}
