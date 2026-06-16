package com.liftlogai.exercise.service;

import com.liftlogai.auth.security.AuthenticatedUser;
import com.liftlogai.common.error.AppException;
import com.liftlogai.exercise.dto.ExerciseProgressionResponse;
import com.liftlogai.exercise.dto.ExerciseResponse;
import com.liftlogai.exercise.dto.ExerciseSummaryResponse;
import com.liftlogai.exercise.entity.Exercise;
import com.liftlogai.exercise.repository.ExerciseRepository;
import com.liftlogai.workout.repository.WorkoutExerciseRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;

    public ExerciseService(
            ExerciseRepository exerciseRepository,
            WorkoutExerciseRepository workoutExerciseRepository
    ) {
        this.exerciseRepository = exerciseRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
    }

    @Transactional(readOnly = true)
    public List<ExerciseResponse> listActiveExercises() {
        return exerciseRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExerciseResponse getExercise(Long id) {
        return toResponse(findActiveExercise(id));
    }

    @Transactional(readOnly = true)
    public ExerciseProgressionResponse getProgression(Long exerciseId, AuthenticatedUser user) {
        Exercise exercise = findActiveExercise(exerciseId);
        List<Object[]> aggregateRows = workoutExerciseRepository.progressionAggregate(user.id(), exerciseId);
        Object[] aggregate = aggregateRows.isEmpty() ? null : aggregateRows.getFirst();

        long workoutCount = aggregate == null || aggregate[0] == null ? 0 : ((Number) aggregate[0]).longValue();
        long completedSetCount = aggregate == null || aggregate[1] == null ? 0 : ((Number) aggregate[1]).longValue();
        Integer bestReps = aggregate == null || aggregate[2] == null ? null : ((Number) aggregate[2]).intValue();
        BigDecimal bestWeight = aggregate == null ? null : toBigDecimal(aggregate[3]);
        BigDecimal totalVolume = aggregate == null ? BigDecimal.ZERO : zeroIfNull(toBigDecimal(aggregate[4]));

        return new ExerciseProgressionResponse(
                new ExerciseSummaryResponse(exercise.getId(), exercise.getName()),
                workoutCount,
                completedSetCount,
                bestReps,
                bestWeight,
                totalVolume
        );
    }

    private Exercise findActiveExercise(Long id) {
        return exerciseRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND,
                        "EXERCISE_NOT_FOUND",
                        "Exercise was not found."
                ));
    }

    private ExerciseResponse toResponse(Exercise exercise) {
        return new ExerciseResponse(
                exercise.getId(),
                exercise.getName(),
                exercise.getCategory(),
                exercise.getPrimaryMuscleGroup(),
                exercise.getMeasurementType()
        );
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        return BigDecimal.valueOf(((Number) value).doubleValue());
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
