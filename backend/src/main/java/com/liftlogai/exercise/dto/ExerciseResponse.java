package com.liftlogai.exercise.dto;

public record ExerciseResponse(
        Long id,
        String name,
        String category,
        String primaryMuscleGroup,
        String measurementType
) {
}
