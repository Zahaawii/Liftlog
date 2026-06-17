package com.liftlogai.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.liftlogai.ai.repository.AiFeedbackRepository;
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
import java.time.LocalDate;
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
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GoalCheckInRepository goalCheckInRepository;

    @Autowired
    private GoalRepository goalRepository;

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

    @Autowired
    private AiFeedbackRepository aiFeedbackRepository;

    @BeforeEach
    void setUp() {
        aiFeedbackRepository.deleteAll();
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
    void summaryComposesAuthenticatedUserFitnessData() throws Exception {
        LocalDate today = LocalDate.now();
        Exercise exercise = exercise();
        AuthCookies owner = register("dashboard-owner@example.com");
        AuthCookies otherUser = register("dashboard-other@example.com");

        createWorkout(owner, exercise.getId(), today.toString(), "Owner Workout");
        createWorkout(otherUser, exercise.getId(), today.toString(), "Other Workout");
        createNutritionLog(owner, today.toString(), "Owner Meal", 650);
        createNutritionLog(otherUser, today.toString(), "Other Meal", 900);
        createGoal(owner, "Train once", today.toString());
        createGoal(otherUser, "Other Goal", today.toString());

        mockMvc.perform(get("/api/dashboard/summary").cookie(owner.accessCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weeklyWorkoutCount").value(1))
                .andExpect(jsonPath("$.recentWorkouts", hasSize(1)))
                .andExpect(jsonPath("$.recentWorkouts[0].title").value("Owner Workout"))
                .andExpect(jsonPath("$.nutritionToday.calories").value(650))
                .andExpect(jsonPath("$.nutritionToday.protein").value(40.0))
                .andExpect(jsonPath("$.activeGoals", hasSize(1)))
                .andExpect(jsonPath("$.activeGoals[0].title").value("Train once"))
                .andExpect(jsonPath("$.activeGoals[0].currentValue").value(1))
                .andExpect(jsonPath("$.latestAiFeedback").doesNotExist());
    }

    @Test
    void summaryRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTHENTICATION_REQUIRED"));
    }

    private void createWorkout(AuthCookies auth, Long exerciseId, String workoutDate, String title) throws Exception {
        mockMvc.perform(post("/api/workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie(), auth.csrfCookie())
                        .header("X-XSRF-TOKEN", auth.csrfCookie().getValue())
                        .content("""
                                {
                                  "workoutDate": "%s",
                                  "title": "%s",
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
                                """.formatted(workoutDate, title, exerciseId)))
                .andExpect(status().isCreated());
    }

    private void createNutritionLog(AuthCookies auth, String logDate, String foodName, int calories) throws Exception {
        mockMvc.perform(post("/api/nutrition/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie(), auth.csrfCookie())
                        .header("X-XSRF-TOKEN", auth.csrfCookie().getValue())
                        .content("""
                                {
                                  "logDate": "%s",
                                  "mealType": "lunch",
                                  "foodName": "%s",
                                  "servingQuantity": 1.0,
                                  "calories": %d,
                                  "protein": 40.0,
                                  "carbohydrates": 50.0,
                                  "fat": 15.0
                                }
                                """.formatted(logDate, foodName, calories)))
                .andExpect(status().isCreated());
    }

    private void createGoal(AuthCookies auth, String title, String today) throws Exception {
        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie(), auth.csrfCookie())
                        .header("X-XSRF-TOKEN", auth.csrfCookie().getValue())
                        .content("""
                                {
                                  "goalType": "workout_frequency",
                                  "title": "%s",
                                  "targetMetric": "workout_count",
                                  "targetValue": 1,
                                  "currentBaseline": 0,
                                  "startDate": "%s",
                                  "targetDate": "%s"
                                }
                                """.formatted(title, today, today)))
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

    private record AuthCookies(
            Cookie accessCookie,
            Cookie csrfCookie
    ) {
    }
}
