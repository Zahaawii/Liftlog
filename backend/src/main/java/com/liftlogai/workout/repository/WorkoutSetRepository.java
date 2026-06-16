package com.liftlogai.workout.repository;

import com.liftlogai.workout.entity.WorkoutSet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutSetRepository extends JpaRepository<WorkoutSet, Long> {

    long countByWorkoutExerciseExerciseId(Long exerciseId);
}
