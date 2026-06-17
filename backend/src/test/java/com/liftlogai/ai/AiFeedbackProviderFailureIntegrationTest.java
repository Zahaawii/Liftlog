package com.liftlogai.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.liftlogai.ai.repository.AiFeedbackRepository;
import com.liftlogai.auth.repository.RefreshTokenRepository;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
        "liftlog.ai.provider=openai",
        "liftlog.ai.openai.api-key="
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AiFeedbackProviderFailureIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AiFeedbackRepository aiFeedbackRepository;

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
    void openAiProviderWithoutApiKeyReturnsStableUnavailableError() throws Exception {
        AuthCookies auth = register("ai-provider-failure@example.com");

        mockMvc.perform(post("/api/ai/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie(), auth.csrfCookie())
                        .header("X-XSRF-TOKEN", auth.csrfCookie().getValue())
                        .content("""
                                {
                                  "requestType": "weekly_review",
                                  "question": "What should I focus on?",
                                  "includeWorkouts": true,
                                  "includeNutrition": true,
                                  "includeGoals": true
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.errorCode").value("AI_PROVIDER_NOT_CONFIGURED"))
                .andExpect(jsonPath("$.message").value("AI feedback is not configured."));

        assertThat(aiFeedbackRepository.count()).isZero();
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
