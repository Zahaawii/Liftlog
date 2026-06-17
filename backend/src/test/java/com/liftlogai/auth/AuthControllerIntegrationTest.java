package com.liftlogai.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.liftlogai.ai.repository.AiFeedbackRepository;
import com.liftlogai.auth.repository.RefreshTokenRepository;
import com.liftlogai.goal.repository.GoalCheckInRepository;
import com.liftlogai.goal.repository.GoalRepository;
import com.liftlogai.nutrition.repository.NutritionLogRepository;
import com.liftlogai.user.entity.User;
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
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private NutritionLogRepository nutritionLogRepository;

    @Autowired
    private GoalCheckInRepository goalCheckInRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private AiFeedbackRepository aiFeedbackRepository;

    @BeforeEach
    void setUp() {
        aiFeedbackRepository.deleteAll();
        goalCheckInRepository.deleteAll();
        goalRepository.deleteAll();
        nutritionLogRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerCreatesUserWithBcryptHashAndSecureHttpOnlyCookies() throws Exception {
        MvcResult result = register("alex@example.com", "strong-password", "Alex")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.email").value("alex@example.com"))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(cookie().httpOnly("liftlog_access", true))
                .andExpect(cookie().secure("liftlog_access", true))
                .andExpect(cookie().maxAge("liftlog_access", 900))
                .andExpect(cookie().httpOnly("liftlog_refresh", true))
                .andExpect(cookie().secure("liftlog_refresh", true))
                .andExpect(cookie().maxAge("liftlog_refresh", 604800))
                .andExpect(cookie().httpOnly("XSRF-TOKEN", false))
                .andReturn();

        User user = userRepository.findByEmail("alex@example.com").orElseThrow();
        assertThat(user.getPasswordHash()).startsWith("$2");
        assertThat(user.getPasswordHash()).isNotEqualTo("strong-password");
        assertThat(result.getResponse().getContentAsString()).doesNotContain("liftlog_access");
        assertThat(result.getResponse().getContentAsString()).doesNotContain("liftlog_refresh");
    }

    @Test
    void registerRejectsDuplicateEmailWithStandardError() throws Exception {
        register("alex@example.com", "strong-password", "Alex").andExpect(status().isCreated());

        register("ALEX@example.com", "another-password", "Alex")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.errorCode").value("AUTH_EMAIL_ALREADY_REGISTERED"))
                .andExpect(jsonPath("$.path").value("/api/auth/register"));
    }

    @Test
    void loginSetsCookiesWithoutReturningTokenMaterial() throws Exception {
        register("alex@example.com", "strong-password", "Alex").andExpect(status().isCreated());

        login("alex@example.com", "strong-password")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("alex@example.com"))
                .andExpect(jsonPath("$", not(hasKey("accessToken"))))
                .andExpect(jsonPath("$", not(hasKey("refreshToken"))))
                .andExpect(cookie().httpOnly("liftlog_access", true))
                .andExpect(cookie().httpOnly("liftlog_refresh", true));
    }

    @Test
    void loginFailureDoesNotRevealWhetherEmailExists() throws Exception {
        login("unknown@example.com", "wrong-password")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    @Test
    void meReturnsCurrentUserWhenAccessCookieIsValid() throws Exception {
        MvcResult registerResult = register("alex@example.com", "strong-password", "Alex")
                .andExpect(status().isCreated())
                .andReturn();

        mockMvc.perform(get("/api/auth/me")
                        .cookie(requiredCookie(registerResult, "liftlog_access")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("alex@example.com"))
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }

    @Test
    void refreshUsesRefreshCookieAndIssuesNewCookies() throws Exception {
        MvcResult registerResult = register("alex@example.com", "strong-password", "Alex")
                .andExpect(status().isCreated())
                .andReturn();

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(requiredCookie(registerResult, "liftlog_refresh")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("alex@example.com"))
                .andExpect(cookie().httpOnly("liftlog_access", true))
                .andExpect(cookie().httpOnly("liftlog_refresh", true))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @Test
    void protectedPostRequiresCsrfToken() throws Exception {
        MvcResult registerResult = register("alex@example.com", "strong-password", "Alex")
                .andExpect(status().isCreated())
                .andReturn();

        Cookie accessCookie = requiredCookie(registerResult, "liftlog_access");
        Cookie refreshCookie = requiredCookie(registerResult, "liftlog_refresh");
        Cookie csrfCookie = requiredCookie(registerResult, "XSRF-TOKEN");

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(accessCookie, refreshCookie, csrfCookie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("CSRF_INVALID"));

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(accessCookie, refreshCookie, csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue()))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("liftlog_access", 0))
                .andExpect(cookie().maxAge("liftlog_refresh", 0));
    }

    @Test
    void validationErrorsUseStandardResponseShape() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "not-an-email",
                          "password": "short"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.path").value("/api/auth/register"));
    }

    private org.springframework.test.web.servlet.ResultActions register(
            String email,
            String password,
            String displayName
    ) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "%s",
                          "password": "%s",
                          "displayName": "%s"
                        }
                        """.formatted(email, password, displayName)));
    }

    private org.springframework.test.web.servlet.ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "%s",
                          "password": "%s"
                        }
                        """.formatted(email, password)));
    }

    private Cookie requiredCookie(MvcResult result, String name) {
        Cookie cookie = result.getResponse().getCookie(name);
        assertThat(cookie).as("Expected cookie %s", name).isNotNull();
        return cookie;
    }
}
