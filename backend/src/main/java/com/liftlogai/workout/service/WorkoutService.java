package com.liftlogai.workout.service;

import com.liftlogai.auth.security.AuthenticatedUser;
import com.liftlogai.common.dto.PagedResponse;
import com.liftlogai.common.error.AppException;
import com.liftlogai.exercise.dto.ExerciseSummaryResponse;
import com.liftlogai.exercise.entity.Exercise;
import com.liftlogai.exercise.repository.ExerciseRepository;
import com.liftlogai.user.entity.User;
import com.liftlogai.user.repository.UserRepository;
import com.liftlogai.workout.dto.WorkoutExerciseRequest;
import com.liftlogai.workout.dto.WorkoutExerciseResponse;
import com.liftlogai.workout.dto.WorkoutRequest;
import com.liftlogai.workout.dto.WorkoutResponse;
import com.liftlogai.workout.dto.WorkoutSetRequest;
import com.liftlogai.workout.dto.WorkoutSetResponse;
import com.liftlogai.workout.entity.Workout;
import com.liftlogai.workout.entity.WorkoutExercise;
import com.liftlogai.workout.entity.WorkoutSet;
import com.liftlogai.workout.repository.WorkoutRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkoutService {

    private static final Logger log = LoggerFactory.getLogger(WorkoutService.class);
    private static final int MAX_PAGE_SIZE = 100;

    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;

    public WorkoutService(
            WorkoutRepository workoutRepository,
            UserRepository userRepository,
            ExerciseRepository exerciseRepository
    ) {
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @Transactional
    public WorkoutResponse createWorkout(AuthenticatedUser authenticatedUser, WorkoutRequest request) {
        User user = findUser(authenticatedUser);
        Workout workout = new Workout(
                user,
                request.workoutDate(),
                clean(request.title()),
                clean(request.notes())
        );
        workout.replaceExercises(toWorkoutExercises(request.exercises()));

        Workout saved = workoutRepository.save(workout);
        log.info("Workout created userId={} workoutId={}", authenticatedUser.id(), saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<WorkoutResponse> listWorkouts(AuthenticatedUser authenticatedUser, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "workoutDate").and(Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return PagedResponse.from(workoutRepository.findByUserId(authenticatedUser.id(), pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public WorkoutResponse getWorkout(AuthenticatedUser authenticatedUser, Long workoutId) {
        return toResponse(findOwnedWorkout(authenticatedUser.id(), workoutId));
    }

    @Transactional
    public WorkoutResponse updateWorkout(AuthenticatedUser authenticatedUser, Long workoutId, WorkoutRequest request) {
        Workout workout = findOwnedWorkout(authenticatedUser.id(), workoutId);
        workout.update(request.workoutDate(), clean(request.title()), clean(request.notes()));
        workout.replaceExercises(toWorkoutExercises(request.exercises()));

        Workout saved = workoutRepository.save(workout);
        log.info("Workout updated userId={} workoutId={}", authenticatedUser.id(), saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public void deleteWorkout(AuthenticatedUser authenticatedUser, Long workoutId) {
        Workout workout = findOwnedWorkout(authenticatedUser.id(), workoutId);
        workoutRepository.delete(workout);
        log.info("Workout deleted userId={} workoutId={}", authenticatedUser.id(), workoutId);
    }

    private User findUser(AuthenticatedUser authenticatedUser) {
        return userRepository.findById(authenticatedUser.id())
                .orElseThrow(() -> new AppException(
                        HttpStatus.UNAUTHORIZED,
                        "AUTHENTICATION_REQUIRED",
                        "Authentication is required."
                ));
    }

    private Workout findOwnedWorkout(Long userId, Long workoutId) {
        return workoutRepository.findByIdAndUserId(workoutId, userId)
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND,
                        "WORKOUT_NOT_FOUND",
                        "Workout was not found."
                ));
    }

    private List<WorkoutExercise> toWorkoutExercises(List<WorkoutExerciseRequest> requests) {
        for (int i = 0; i < requests.size(); i++) {
            validateExerciseRequest(requests.get(i));
        }

        return java.util.stream.IntStream.range(0, requests.size())
                .mapToObj(index -> toWorkoutExercise(index + 1, requests.get(index)))
                .toList();
    }

    private WorkoutExercise toWorkoutExercise(int displayOrder, WorkoutExerciseRequest request) {
        Exercise exercise = exerciseRepository.findByIdAndActiveTrue(request.exerciseId())
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND,
                        "EXERCISE_NOT_FOUND",
                        "Exercise was not found."
                ));
        WorkoutExercise workoutExercise = new WorkoutExercise(exercise, displayOrder, clean(request.notes()));
        workoutExercise.replaceSets(request.sets().stream().map(this::toWorkoutSet).toList());
        return workoutExercise;
    }

    private WorkoutSet toWorkoutSet(WorkoutSetRequest request) {
        return new WorkoutSet(
                request.setNumber(),
                request.reps(),
                request.weight(),
                request.durationSeconds(),
                request.distance(),
                request.completed() == null || request.completed(),
                clean(request.notes())
        );
    }

    private void validateExerciseRequest(WorkoutExerciseRequest request) {
        request.sets().forEach(this::validateSetRequest);
    }

    private void validateSetRequest(WorkoutSetRequest request) {
        if (request.reps() == null
                && request.weight() == null
                && request.durationSeconds() == null
                && request.distance() == null) {
            throw new AppException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "WORKOUT_SET_METRIC_REQUIRED",
                    "Each workout set requires at least one performance metric."
            );
        }
    }

    private WorkoutResponse toResponse(Workout workout) {
        return new WorkoutResponse(
                workout.getId(),
                workout.getWorkoutDate(),
                workout.getTitle(),
                workout.getNotes(),
                workout.getExercises().stream().map(this::toExerciseResponse).toList(),
                workout.getCreatedAt(),
                workout.getUpdatedAt()
        );
    }

    private WorkoutExerciseResponse toExerciseResponse(WorkoutExercise workoutExercise) {
        Exercise exercise = workoutExercise.getExercise();
        return new WorkoutExerciseResponse(
                workoutExercise.getId(),
                new ExerciseSummaryResponse(exercise.getId(), exercise.getName()),
                workoutExercise.getDisplayOrder(),
                workoutExercise.getNotes(),
                workoutExercise.getSets().stream().map(this::toSetResponse).toList()
        );
    }

    private WorkoutSetResponse toSetResponse(WorkoutSet workoutSet) {
        return new WorkoutSetResponse(
                workoutSet.getId(),
                workoutSet.getSetNumber(),
                workoutSet.getReps(),
                workoutSet.getWeight(),
                workoutSet.getDurationSeconds(),
                workoutSet.getDistance(),
                workoutSet.isCompleted(),
                workoutSet.getNotes()
        );
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
