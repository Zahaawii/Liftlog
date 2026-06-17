package com.liftlogai.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.liftlogai.ai.provider.AiProvider;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AiFeedbackControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AiProvider aiProvider;

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
    void testProfileUsesStubProvider() {
        assertThat(aiProvider.providerName()).isEqualTo("stub");
    }

    @Test
    void requestFeedbackUsesStubProviderAndPersistsHistory() throws Exception {
        AuthCookies auth = register("ai-owner@example.com");

        mockMvc.perform(post("/api/ai/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie(), auth.csrfCookie())
                        .header("X-XSRF-TOKEN", auth.csrfCookie().getValue())
                        .content(feedbackJson("weekly_review", "What should I focus on next week?")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.provider").value("stub"))
                .andExpect(jsonPath("$.promptSummary").value("weekly_review feedback using recent workouts, nutrition totals, active goals. Custom question included."))
                .andExpect(jsonPath("$.summary").value("Your recent fitness data has enough structure for a practical review."))
                .andExpect(jsonPath("$.recommendations", hasSize(2)))
                .andExpect(jsonPath("$.disclaimer").value("This feedback is informational and is not medical advice."))
                .andExpect(jsonPath("$.feedback").isNotEmpty());

        assertThat(aiFeedbackRepository.count()).isEqualTo(1);
        assertThat(aiFeedbackRepository.findAll().getFirst().getPromptSummary()).doesNotContain("What should I focus");
    }

    @Test
    void feedbackHistoryIsPaginatedAndScopedToCurrentUser() throws Exception {
        AuthCookies owner = register("ai-history@example.com");
        AuthCookies otherUser = register("other-ai@example.com");

        Long ownerFeedbackId = createFeedback(owner, "weekly_review");
        Long otherFeedbackId = createFeedback(otherUser, "goal_review");

        mockMvc.perform(get("/api/ai/feedback?page=0&size=10").cookie(owner.accessCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(ownerFeedbackId))
                .andExpect(jsonPath("$.items[0].requestType").value("weekly_review"))
                .andExpect(jsonPath("$.totalItems").value(1));

        mockMvc.perform(get("/api/ai/feedback/{id}", otherFeedbackId).cookie(owner.accessCookie()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("AI_FEEDBACK_NOT_FOUND"));
    }

    @Test
    void dashboardIncludesLatestAiFeedback() throws Exception {
        AuthCookies auth = register("ai-dashboard@example.com");
        Long feedbackId = createFeedback(auth, "weekly_review");

        mockMvc.perform(get("/api/dashboard/summary").cookie(auth.accessCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latestAiFeedback.id").value(feedbackId))
                .andExpect(jsonPath("$.latestAiFeedback.provider").value("stub"));
    }

    @Test
    void feedbackMutationRequiresCsrfToken() throws Exception {
        AuthCookies auth = register("ai-csrf@example.com");

        mockMvc.perform(post("/api/ai/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie())
                        .content(feedbackJson("weekly_review", "Missing CSRF")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("CSRF_INVALID"));
    }

    private Long createFeedback(AuthCookies auth, String requestType) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/ai/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie(), auth.csrfCookie())
                        .header("X-XSRF-TOKEN", auth.csrfCookie().getValue())
                        .content(feedbackJson(requestType, "Give me advice.")))
                .andExpect(status().isCreated())
                .andReturn();
        return Long.valueOf(com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id").toString());
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

    private String feedbackJson(String requestType, String question) {
        return """
                {
                  "requestType": "%s",
                  "question": "%s",
                  "includeWorkouts": true,
                  "includeNutrition": true,
                  "includeGoals": true
                }
                """.formatted(requestType, question);
    }

    private record AuthCookies(
            Cookie accessCookie,
            Cookie csrfCookie
    ) {
    }
}
