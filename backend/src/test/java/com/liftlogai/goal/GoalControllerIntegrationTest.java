package com.liftlogai.goal;

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
class GoalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private GoalCheckInRepository goalCheckInRepository;

    @Autowired
    private NutritionLogRepository nutritionLogRepository;

    @Autowired
    private WorkoutSetRepository workoutSetRepository;

    @Autowired
    private WorkoutExerciseRepository workoutExerciseRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        goalCheckInRepository.deleteAll();
        goalRepository.deleteAll();
        nutritionLogRepository.deleteAll();
        workoutSetRepository.deleteAll();
        workoutExerciseRepository.deleteAll();
        workoutRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        exerciseRepository.deleteAll();
    }

    @Test
    void createGoalReturnsDeterministicWorkoutProgress() throws Exception {
        Exercise exercise = exercise();
        AuthCookies auth = register("goal-owner@example.com");
        createWorkout(auth, exercise.getId(), "2026-06-16");
        createWorkout(auth, exercise.getId(), "2026-06-16");

        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie(), auth.csrfCookie())
                        .header("X-XSRF-TOKEN", auth.csrfCookie().getValue())
                        .content(goalJson("workout_frequency", "Train twice", "workout_count", "2", "2026-06-16", "2026-06-16")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goalType").value("workout_frequency"))
                .andExpect(jsonPath("$.targetMetric").value("workout_count"))
                .andExpect(jsonPath("$.currentValue").value(2))
                .andExpect(jsonPath("$.progressPercent").value(100))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        assertThat(goalRepository.count()).isEqualTo(1);
    }

    @Test
    void goalHistoryIsPaginatedAndScopedToCurrentUser() throws Exception {
        AuthCookies owner = register("goal-history@example.com");
        AuthCookies otherUser = register("other-goal@example.com");

        Long ownerGoalId = createGoal(owner, "Owner Goal");
        Long otherGoalId = createGoal(otherUser, "Other Goal");

        mockMvc.perform(get("/api/goals?page=0&size=10").cookie(owner.accessCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(ownerGoalId))
                .andExpect(jsonPath("$.items[0].title").value("Owner Goal"))
                .andExpect(jsonPath("$.totalItems").value(1));

        mockMvc.perform(get("/api/goals/{id}", otherGoalId).cookie(owner.accessCookie()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("GOAL_NOT_FOUND"));
    }

    @Test
    void checkInsDriveGenericGoalProgress() throws Exception {
        AuthCookies auth = register("goal-checkin@example.com");
        Long goalId = createGoal(auth, "Body weight");

        mockMvc.perform(post("/api/goals/{id}/check-ins", goalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie(), auth.csrfCookie())
                        .header("X-XSRF-TOKEN", auth.csrfCookie().getValue())
                        .content("""
                                {
                                  "checkInDate": "2026-06-17",
                                  "value": 75.00,
                                  "notes": "Morning check-in"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.value").value(75.00));

        mockMvc.perform(get("/api/goals/{id}", goalId).cookie(auth.accessCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentValue").value(75.00))
                .andExpect(jsonPath("$.progressPercent").value(94));

        mockMvc.perform(get("/api/goals/{id}/check-ins", goalId).cookie(auth.accessCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].notes").value("Morning check-in"));
    }

    @Test
    void updateAndDeleteGoal() throws Exception {
        AuthCookies auth = register("goal-editor@example.com");
        Long goalId = createGoal(auth, "Original Goal");

        mockMvc.perform(put("/api/goals/{id}", goalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie(), auth.csrfCookie())
                        .header("X-XSRF-TOKEN", auth.csrfCookie().getValue())
                        .content(goalJson("custom", "Updated Goal", "body_weight", "90", "2026-06-16", "2026-07-16")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Goal"))
                .andExpect(jsonPath("$.targetValue").value(90));

        mockMvc.perform(delete("/api/goals/{id}", goalId)
                        .cookie(auth.accessCookie(), auth.csrfCookie())
                        .header("X-XSRF-TOKEN", auth.csrfCookie().getValue()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/goals/{id}", goalId).cookie(auth.accessCookie()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("GOAL_NOT_FOUND"));
    }

    @Test
    void invalidGoalDateRangeUsesDomainError() throws Exception {
        AuthCookies auth = register("goal-invalid@example.com");

        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie(), auth.csrfCookie())
                        .header("X-XSRF-TOKEN", auth.csrfCookie().getValue())
                        .content(goalJson("custom", "Invalid", "body_weight", "80", "2026-07-16", "2026-06-16")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("GOAL_DATE_RANGE_INVALID"))
                .andExpect(jsonPath("$.path").value("/api/goals"));
    }

    @Test
    void goalMutationRequiresCsrfToken() throws Exception {
        AuthCookies auth = register("goal-csrf@example.com");

        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie())
                        .content(goalJson("custom", "Missing CSRF", "body_weight", "80", "2026-06-16", "2026-07-16")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("CSRF_INVALID"));
    }

    private Long createGoal(AuthCookies auth, String title) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie(), auth.csrfCookie())
                        .header("X-XSRF-TOKEN", auth.csrfCookie().getValue())
                        .content(goalJson("custom", title, "body_weight", "80", "2026-06-16", "2026-07-16")))
                .andExpect(status().isCreated())
                .andReturn();
        return Long.valueOf(com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id").toString());
    }

    private void createWorkout(AuthCookies auth, Long exerciseId, String workoutDate) throws Exception {
        mockMvc.perform(post("/api/workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie(), auth.csrfCookie())
                        .header("X-XSRF-TOKEN", auth.csrfCookie().getValue())
                        .content("""
                                {
                                  "workoutDate": "%s",
                                  "title": "Goal source",
                                  "exercises": [
                                    {
                                      "exerciseId": %d,
                                      "sets": [
                                        {
                                          "setNumber": 1,
                                          "reps": 8,
                                          "completed": true
                                        }
                                      ]
                                    }
                                  ]
                                }
                                """.formatted(workoutDate, exerciseId)))
                .andExpect(status().isCreated());
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

    private String goalJson(
            String goalType,
            String title,
            String targetMetric,
            String targetValue,
            String startDate,
            String targetDate
    ) {
        return """
                {
                  "goalType": "%s",
                  "title": "%s",
                  "targetMetric": "%s",
                  "targetValue": %s,
                  "currentBaseline": 70.00,
                  "startDate": "%s",
                  "targetDate": "%s"
                }
                """.formatted(goalType, title, targetMetric, targetValue, startDate, targetDate);
    }

    private record AuthCookies(
            Cookie accessCookie,
            Cookie csrfCookie
    ) {
    }
}
