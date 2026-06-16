package com.liftlogai.workout.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record WorkoutExerciseRequest(
        @NotNull Long exerciseId,
        @Size(max = 1000) String notes,
        @NotEmpty List<@Valid WorkoutSetRequest> sets
) {
}
