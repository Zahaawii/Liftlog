package com.liftlogai.exercise.controller;

import com.liftlogai.auth.security.AuthenticatedUser;
import com.liftlogai.exercise.dto.ExerciseProgressionResponse;
import com.liftlogai.exercise.dto.ExerciseResponse;
import com.liftlogai.exercise.service.ExerciseService;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @GetMapping
    List<ExerciseResponse> listExercises() {
        return exerciseService.listActiveExercises();
    }

    @GetMapping("/{id}")
    ExerciseResponse getExercise(@PathVariable Long id) {
        return exerciseService.getExercise(id);
    }

    @GetMapping("/{id}/progression")
    ExerciseProgressionResponse getProgression(@PathVariable Long id, Authentication authentication) {
        return exerciseService.getProgression(id, (AuthenticatedUser) authentication.getPrincipal());
    }
}
