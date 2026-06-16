package com.liftlogai.workout.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record WorkoutRequest(
        @NotNull LocalDate workoutDate,
        @Size(max = 160) String title,
        @Size(max = 1000) String notes,
        @NotEmpty List<@Valid WorkoutExerciseRequest> exercises
) {
}
