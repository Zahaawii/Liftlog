package com.liftlogai.exercise.service;

import com.liftlogai.exercise.entity.Exercise;
import com.liftlogai.exercise.repository.ExerciseRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ExerciseSeeder implements ApplicationRunner {

    private static final String SYSTEM_SOURCE = "system";

    private final ExerciseRepository exerciseRepository;

    public ExerciseSeeder(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seed("Back Squat", "strength", "legs", "weight_reps");
        seed("Bench Press", "strength", "chest", "weight_reps");
        seed("Deadlift", "strength", "back", "weight_reps");
        seed("Overhead Press", "strength", "shoulders", "weight_reps");
        seed("Pull-Up", "strength", "back", "reps");
        seed("Running", "cardio", "full_body", "duration_distance");
    }

    private void seed(String name, String category, String primaryMuscleGroup, String measurementType) {
        if (!exerciseRepository.existsByName(name)) {
            exerciseRepository.save(new Exercise(name, category, primaryMuscleGroup, measurementType, SYSTEM_SOURCE));
        }
    }
}
