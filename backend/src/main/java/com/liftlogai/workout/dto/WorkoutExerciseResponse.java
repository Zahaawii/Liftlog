package com.liftlogai.workout.dto;

import com.liftlogai.exercise.dto.ExerciseSummaryResponse;
import java.util.List;

public record WorkoutExerciseResponse(
        Long id,
        ExerciseSummaryResponse exercise,
        int displayOrder,
        String notes,
        List<WorkoutSetResponse> sets
) {
}
