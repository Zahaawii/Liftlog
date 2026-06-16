package com.liftlogai.nutrition;

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
import com.liftlogai.goal.repository.GoalCheckInRepository;
import com.liftlogai.goal.repository.GoalRepository;
import com.liftlogai.nutrition.repository.NutritionLogRepository;
import com.liftlogai.user.repository.UserRepository;
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
class NutritionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NutritionLogRepository nutritionLogRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GoalCheckInRepository goalCheckInRepository;

    @Autowired
    private GoalRepository goalRepository;

    @BeforeEach
    void setUp() {
        goalCheckInRepository.deleteAll();
        goalRepository.deleteAll();
        nutritionLogRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createNutritionLogPersistsForCurrentUser() throws Exception {
        AuthCookies auth = register("nutrition-owner@example.com");

        mockMvc.perform(post("/api/nutrition/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie(), auth.csrfCookie())
                        .header("X-XSRF-TOKEN", auth.csrfCookie().getValue())
                        .content(nutritionJson("lunch", "Chicken rice bowl", 650, "45.00", "70.00", "18.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.logDate").value("2026-06-16"))
                .andExpect(jsonPath("$.mealType").value("lunch"))
                .andExpect(jsonPath("$.foodName").value("Chicken rice bowl"))
                .andExpect(jsonPath("$.calories").value(650))
                .andExpect(jsonPath("$.protein").value(45.00))
                .andExpect(jsonPath("$.carbohydrates").value(70.00))
                .andExpect(jsonPath("$.fat").value(18.00));

        assertThat(nutritionLogRepository.count()).isEqualTo(1);
        assertThat(nutritionLogRepository.findAll().getFirst().getFoodName()).isEqualTo("Chicken rice bowl");
    }

    @Test
    void nutritionHistoryIsPaginatedAndScopedToCurrentUser() throws Exception {
        AuthCookies owner = register("nutrition-history@example.com");
        AuthCookies otherUser = register("other-nutrition@example.com");

        Long ownerLogId = createNutritionLog(owner, "breakfast", "Oats", 450);
        Long otherLogId = createNutritionLog(otherUser, "dinner", "Pasta", 900);

        mockMvc.perform(get("/api/nutrition/logs?page=0&size=10").cookie(owner.accessCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(ownerLogId))
                .andExpect(jsonPath("$.items[0].foodName").value("Oats"))
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));

        mockMvc.perform(get("/api/nutrition/logs/{id}", otherLogId).cookie(owner.accessCookie()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NUTRITION_LOG_NOT_FOUND"));
    }

    @Test
    void dailySummaryAggregatesOnlyCurrentUserAndDate() throws Exception {
        AuthCookies owner = register("nutrition-summary@example.com");
        AuthCookies otherUser = register("other-summary@example.com");

        createNutritionLog(owner, "breakfast", "Oats", 450);
        createNutritionLog(owner, "lunch", "Chicken rice bowl", 650);
        createNutritionLog(otherUser, "lunch", "Other bowl", 1000);

        mockMvc.perform(get("/api/nutrition/summary/daily?date=2026-06-16").cookie(owner.accessCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-06-16"))
                .andExpect(jsonPath("$.calories").value(1100))
                .andExpect(jsonPath("$.protein").value(90.00))
                .andExpect(jsonPath("$.carbohydrates").value(140.00))
                .andExpect(jsonPath("$.fat").value(36.00));

        mockMvc.perform(get("/api/nutrition/summary/daily?date=2026-06-17").cookie(owner.accessCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calories").value(0))
                .andExpect(jsonPath("$.protein").value(0));
    }

    @Test
    void updateAndDeleteNutritionLog() throws Exception {
        AuthCookies auth = register("nutrition-editor@example.com");
        Long logId = createNutritionLog(auth, "snack", "Yogurt", 220);

        mockMvc.perform(put("/api/nutrition/logs/{id}", logId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie(), auth.csrfCookie())
                        .header("X-XSRF-TOKEN", auth.csrfCookie().getValue())
                        .content(nutritionJson("dinner", "Salmon bowl", 720, "52.00", "58.00", "25.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mealType").value("dinner"))
                .andExpect(jsonPath("$.foodName").value("Salmon bowl"))
                .andExpect(jsonPath("$.calories").value(720));

        mockMvc.perform(delete("/api/nutrition/logs/{id}", logId)
                        .cookie(auth.accessCookie(), auth.csrfCookie())
                        .header("X-XSRF-TOKEN", auth.csrfCookie().getValue()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/nutrition/logs/{id}", logId).cookie(auth.accessCookie()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NUTRITION_LOG_NOT_FOUND"));
    }

    @Test
    void nutritionLogRequiresAtLeastOneNutritionValue() throws Exception {
        AuthCookies auth = register("nutrition-invalid@example.com");

        mockMvc.perform(post("/api/nutrition/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie(), auth.csrfCookie())
                        .header("X-XSRF-TOKEN", auth.csrfCookie().getValue())
                        .content("""
                                {
                                  "logDate": "2026-06-16",
                                  "mealType": "snack",
                                  "foodName": "Water",
                                  "servingQuantity": 1.0
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.errorCode").value("NUTRITION_VALUE_REQUIRED"))
                .andExpect(jsonPath("$.path").value("/api/nutrition/logs"));
    }

    @Test
    void nutritionValidationUsesStandardErrorShape() throws Exception {
        AuthCookies auth = register("nutrition-validation@example.com");

        mockMvc.perform(post("/api/nutrition/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie(), auth.csrfCookie())
                        .header("X-XSRF-TOKEN", auth.csrfCookie().getValue())
                        .content("""
                                {
                                  "logDate": "2026-06-16",
                                  "mealType": "snack",
                                  "foodName": "Invalid",
                                  "calories": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.path").value("/api/nutrition/logs"));
    }

    @Test
    void nutritionMutationRequiresCsrfToken() throws Exception {
        AuthCookies auth = register("nutrition-csrf@example.com");

        mockMvc.perform(post("/api/nutrition/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie())
                        .content(nutritionJson("lunch", "Chicken rice bowl", 650, "45.00", "70.00", "18.00")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("CSRF_INVALID"));
    }

    private Long createNutritionLog(AuthCookies auth, String mealType, String foodName, int calories) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/nutrition/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(auth.accessCookie(), auth.csrfCookie())
                        .header("X-XSRF-TOKEN", auth.csrfCookie().getValue())
                        .content(nutritionJson(mealType, foodName, calories, "45.00", "70.00", "18.00")))
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

    private String nutritionJson(
            String mealType,
            String foodName,
            int calories,
            String protein,
            String carbohydrates,
            String fat
    ) {
        return """
                {
                  "logDate": "2026-06-16",
                  "mealType": "%s",
                  "foodName": "%s",
                  "servingQuantity": 1.0,
                  "calories": %d,
                  "protein": %s,
                  "carbohydrates": %s,
                  "fat": %s,
                  "notes": "Test nutrition entry"
                }
                """.formatted(mealType, foodName, calories, protein, carbohydrates, fat);
    }

    private record AuthCookies(
            Cookie accessCookie,
            Cookie csrfCookie
    ) {
    }
}
