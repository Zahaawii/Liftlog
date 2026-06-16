package com.liftlogai.workout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.liftlogai.auth.repository.RefreshTokenRepository;
import com.liftlogai.exercise.entity.Exercise;
import com.liftlogai.exercise.repository.ExerciseRepository;
import com.liftlogai.goal.repository.GoalCheckInRepository;
import com.liftlogai.goal.repository.GoalRepository;
import com.liftlogai.nutrition.repository.NutritionLogRepository;
import com.liftlogai.user.repository.UserRepository;
import com.liftlogai.workout.repository.WorkoutExerciseRepository;
import com.liftlogai.workout.repository.WorkoutRepository;
import com.liftlogai.workout.repository.WorkoutSetRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkoutControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private WorkoutExerciseRepository workoutExerciseRepository;

    @Autowired
    private WorkoutSetRepository workoutSetRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private NutritionLogRepository nutritionLogRepository;

    @Autowired
    private GoalCheckInRepository goalCheckInRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        workoutSetRepository.deleteAll();
        workoutExerciseRepository.deleteAll();
        workoutRepository.deleteAll();
        goalCheckInRepository.deleteAll();
        goalRepository.deleteAll();
        nutritionLogRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        exerciseRepository.deleteAll();
    }

    @Test
    void createWorkoutPersistsNestedSetsAndExerciseReference() throws Exception {
        Exercise exercise = exercise();
        AuthCookies auth = register("workout-owner@example.com");

        mockMvc.perform(post("/api/workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie(), auth.csrfCookie())
                        .header("X-XSRF-TOKEN", auth.csrfCookie().getValue())
                        .content(workoutJson(exercise.getId(), "Upper Body", 8, "80.50")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Upper Body"))
                .andExpect(jsonPath("$.exercises[0].exercise.id").value(exercise.getId()))
                .andExpect(jsonPath("$.exercises[0].exercise.name").value("Bench Press"))
                .andExpect(jsonPath("$.exercises[0].sets[0].reps").value(8))
                .andExpect(jsonPath("$.exercises[0].sets[0].completed").value(true));

        assertThat(workoutRepository.count()).isEqualTo(1);
        assertThat(workoutExerciseRepository.countByExerciseId(exercise.getId())).isEqualTo(1);
        assertThat(workoutSetRepository.countByWorkoutExerciseExerciseId(exercise.getId())).isEqualTo(1);
    }

    @Test
    void workoutHistoryIsPaginatedAndScopedToCurrentUser() throws Exception {
        Exercise exercise = exercise();
        AuthCookies owner = register("owner@example.com");
        AuthCookies otherUser = register("other@example.com");

        Long ownerWorkoutId = createWorkout(owner, exercise.getId(), "Owner Workout");
        Long otherWorkoutId = createWorkout(otherUser, exercise.getId(), "Other Workout");

        mockMvc.perform(get("/api/workouts?page=0&size=10").cookie(owner.accessCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(ownerWorkoutId))
                .andExpect(jsonPath("$.items[0].title").value("Owner Workout"))
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));

        mockMvc.perform(get("/api/workouts/{id}", otherWorkoutId).cookie(owner.accessCookie()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("WORKOUT_NOT_FOUND"));
    }

    @Test
    void updateWorkoutReplacesNestedSetsAndDeleteRemovesWorkout() throws Exception {
        Exercise exercise = exercise();
        AuthCookies auth = register("editor@example.com");
        Long workoutId = createWorkout(auth, exercise.getId(), "Initial");

        mockMvc.perform(put("/api/workouts/{id}", workoutId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie(), auth.csrfCookie())
                        .header("X-XSRF-TOKEN", auth.csrfCookie().getValue())
                        .content(workoutJson(exercise.getId(), "Updated", 5, "100.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"))
                .andExpect(jsonPath("$.exercises[0].sets", hasSize(1)))
                .andExpect(jsonPath("$.exercises[0].sets[0].reps").value(5))
                .andExpect(jsonPath("$.exercises[0].sets[0].weight").value(100.00));

        mockMvc.perform(delete("/api/workouts/{id}", workoutId)
                        .cookie(auth.accessCookie(), auth.csrfCookie())
                        .header("X-XSRF-TOKEN", auth.csrfCookie().getValue()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/workouts/{id}", workoutId).cookie(auth.accessCookie()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("WORKOUT_NOT_FOUND"));
    }

    @Test
    void workoutSetRequiresPerformanceMetric() throws Exception {
        Exercise exercise = exercise();
        AuthCookies auth = register("invalid@example.com");

        mockMvc.perform(post("/api/workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie(), auth.csrfCookie())
                        .header("X-XSRF-TOKEN", auth.csrfCookie().getValue())
                        .content("""
                                {
                                  "workoutDate": "2026-06-16",
                                  "title": "Invalid",
                                  "exercises": [
                                    {
                                      "exerciseId": %d,
                                      "sets": [
                                        {
                                          "setNumber": 1,
                                          "completed": true
                                        }
                                      ]
                                    }
                                  ]
                                }
                                """.formatted(exercise.getId())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.errorCode").value("WORKOUT_SET_METRIC_REQUIRED"))
                .andExpect(jsonPath("$.path").value("/api/workouts"));
    }

    @Test
    void workoutMutationRequiresCsrfToken() throws Exception {
        Exercise exercise = exercise();
        AuthCookies auth = register("csrf@example.com");

        mockMvc.perform(post("/api/workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie())
                        .content(workoutJson(exercise.getId(), "Missing CSRF", 8, "75.00")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("CSRF_INVALID"));
    }

    @Test
    void exerciseProgressionUsesOnlyCurrentUserCompletedSets() throws Exception {
        Exercise exercise = exercise();
        AuthCookies owner = register("progress@example.com");
        AuthCookies otherUser = register("other-progress@example.com");

        createWorkout(owner, exercise.getId(), "Progress Source");
        createWorkout(otherUser, exercise.getId(), "Other Source");

        mockMvc.perform(get("/api/exercises/{id}/progression", exercise.getId()).cookie(owner.accessCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exercise.id").value(exercise.getId()))
                .andExpect(jsonPath("$.workoutCount").value(1))
                .andExpect(jsonPath("$.completedSetCount").value(1))
                .andExpect(jsonPath("$.bestReps").value(8))
                .andExpect(jsonPath("$.bestWeight").value(80.50))
                .andExpect(jsonPath("$.totalVolume").value(644.00));
    }

    @Test
    void exerciseListRequiresAuthenticationAndReturnsSharedExercises() throws Exception {
        Exercise exercise = exercise();
        AuthCookies auth = register("exercise-list@example.com");

        mockMvc.perform(get("/api/exercises"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/exercises").cookie(auth.accessCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(exercise.getId()))
                .andExpect(jsonPath("$[0].name").value("Bench Press"));
    }

    private Long createWorkout(AuthCookies auth, Long exerciseId, String title) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie(), auth.csrfCookie())
                        .header("X-XSRF-TOKEN", auth.csrfCookie().getValue())
                        .content(workoutJson(exerciseId, title, 8, "80.50")))
                .andExpect(status().isCreated())
                .andReturn();
        return Long.valueOf(com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id").toString());
    }

    private Exercise exercise() {
        return exerciseRepository.save(new Exercise("Bench Press", "strength", "chest", "weight_reps", "test"));
    }

    private AuthCookies register(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "strong-password",
                                  "displayName": "Test User"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(cookie().httpOnly("liftlog_access", true))
                .andReturn();
        return new AuthCookies(
                requiredCookie(result, "liftlog_access"),
                requiredCookie(result, "XSRF-TOKEN")
        );
    }

    private Cookie requiredCookie(MvcResult result, String name) {
        Cookie cookie = result.getResponse().getCookie(name);
        assertThat(cookie).as("Expected cookie %s", name).isNotNull();
        return cookie;
    }

    private String workoutJson(Long exerciseId, String title, int reps, String weight) {
        return """
                {
                  "workoutDate": "2026-06-16",
                  "title": "%s",
                  "notes": "Felt strong",
                  "exercises": [
                    {
                      "exerciseId": %d,
                      "notes": "Controlled tempo",
                      "sets": [
                        {
                          "setNumber": 1,
                          "reps": %d,
                          "weight": %s,
                          "completed": true,
                          "notes": "Clean rep"
                        }
                      ]
                    }
                  ]
                }
                """.formatted(title, exerciseId, reps, weight);
    }

    private record AuthCookies(
            Cookie accessCookie,
            Cookie csrfCookie
    ) {
    }
}
