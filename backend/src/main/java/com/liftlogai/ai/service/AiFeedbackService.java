package com.liftlogai.ai.service;
import com.liftlogai.ai.dto.AiFeedbackRequest;
import com.liftlogai.ai.dto.AiFeedbackResponse;
import com.liftlogai.ai.entity.AiFeedback;
import com.liftlogai.ai.entity.AiFeedbackStatus;
import com.liftlogai.ai.prompt.PromptBuildResult;
import com.liftlogai.ai.prompt.PromptBuilder;
import com.liftlogai.ai.prompt.PromptContext;
import com.liftlogai.ai.provider.AiProvider;
import com.liftlogai.ai.provider.AiProviderException;
import com.liftlogai.ai.provider.AiProviderRequest;
import com.liftlogai.ai.provider.AiProviderResponse;
import com.liftlogai.ai.repository.AiFeedbackRepository;
import com.liftlogai.auth.security.AuthenticatedUser;
import com.liftlogai.common.dto.PagedResponse;
import com.liftlogai.common.error.AppException;
import com.liftlogai.goal.dto.GoalResponse;
import com.liftlogai.goal.service.GoalService;
import com.liftlogai.nutrition.dto.DailyNutritionSummaryResponse;
import com.liftlogai.nutrition.service.NutritionService;
import com.liftlogai.user.entity.User;
import com.liftlogai.user.repository.UserRepository;
import com.liftlogai.workout.dto.WorkoutResponse;
import com.liftlogai.workout.service.WorkoutService;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiFeedbackService {

    private static final Logger log = LoggerFactory.getLogger(AiFeedbackService.class);
    private static final int MAX_PAGE_SIZE = 100;
    private static final String DISCLAIMER = "This feedback is informational and is not medical advice.";

    private final AiFeedbackRepository aiFeedbackRepository;
    private final UserRepository userRepository;
    private final WorkoutService workoutService;
    private final NutritionService nutritionService;
    private final GoalService goalService;
    private final PromptBuilder promptBuilder;
    private final AiProvider aiProvider;

    public AiFeedbackService(
            AiFeedbackRepository aiFeedbackRepository,
            UserRepository userRepository,
            WorkoutService workoutService,
            NutritionService nutritionService,
            GoalService goalService,
            PromptBuilder promptBuilder,
            AiProvider aiProvider
    ) {
        this.aiFeedbackRepository = aiFeedbackRepository;
        this.userRepository = userRepository;
        this.workoutService = workoutService;
        this.nutritionService = nutritionService;
        this.goalService = goalService;
        this.promptBuilder = promptBuilder;
        this.aiProvider = aiProvider;
    }

    @Transactional
    public AiFeedbackResponse requestFeedback(AuthenticatedUser authenticatedUser, AiFeedbackRequest request) {
        User user = findUser(authenticatedUser);
        PromptBuildResult prompt = promptBuilder.build(request, promptContext(authenticatedUser));

        AiProviderResponse providerResponse;
        try {
            providerResponse = aiProvider.generateFeedback(new AiProviderRequest(prompt.prompt(), prompt.promptSummary()));
        } catch (AiProviderException exception) {
            log.warn("AI provider failure userId={} provider={} errorCode={}",
                    authenticatedUser.id(),
                    aiProvider.providerName(),
                    exception.errorCode());
            throw new AppException(providerStatus(exception), exception.errorCode(), userSafeProviderMessage(exception));
        }

        AiFeedback feedback = aiFeedbackRepository.save(new AiFeedback(
                user,
                normalizeRequestType(request.requestType()),
                aiProvider.providerName(),
                AiFeedbackStatus.SUCCESS,
                prompt.promptSummary(),
                cleanRequired(providerResponse.summary(), "AI feedback generated."),
                recommendationsJson(providerResponse.recommendations()),
                cleanRequired(providerResponse.feedback(), providerResponse.summary())
        ));
        log.info("AI feedback created userId={} aiFeedbackId={} provider={}",
                authenticatedUser.id(),
                feedback.getId(),
                aiProvider.providerName());
        return toResponse(feedback);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AiFeedbackResponse> listFeedback(AuthenticatedUser authenticatedUser, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return PagedResponse.from(aiFeedbackRepository.findByUserId(authenticatedUser.id(), pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public AiFeedbackResponse getFeedback(AuthenticatedUser authenticatedUser, Long feedbackId) {
        return toResponse(findOwnedFeedback(authenticatedUser.id(), feedbackId));
    }

    @Transactional(readOnly = true)
    public AiFeedbackResponse latestFeedback(AuthenticatedUser authenticatedUser) {
        return aiFeedbackRepository.findFirstByUserIdOrderByCreatedAtDesc(authenticatedUser.id())
                .map(this::toResponse)
                .orElse(null);
    }

    private PromptContext promptContext(AuthenticatedUser authenticatedUser) {
        List<WorkoutResponse> recentWorkouts = workoutService.listWorkouts(authenticatedUser, 0, 5).items();
        DailyNutritionSummaryResponse nutritionToday = nutritionService.dailySummary(authenticatedUser, LocalDate.now());
        List<GoalResponse> activeGoals = goalService.activeGoals(authenticatedUser);
        return new PromptContext(recentWorkouts, nutritionToday, activeGoals);
    }

    private User findUser(AuthenticatedUser authenticatedUser) {
        return userRepository.findById(authenticatedUser.id())
                .orElseThrow(() -> new AppException(
                        HttpStatus.UNAUTHORIZED,
                        "AUTHENTICATION_REQUIRED",
                        "Authentication is required."
                ));
    }

    private AiFeedback findOwnedFeedback(Long userId, Long feedbackId) {
        return aiFeedbackRepository.findByIdAndUserId(feedbackId, userId)
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND,
                        "AI_FEEDBACK_NOT_FOUND",
                        "AI feedback was not found."
                ));
    }

    private AiFeedbackResponse toResponse(AiFeedback feedback) {
        return new AiFeedbackResponse(
                feedback.getId(),
                feedback.getUser().getId(),
                feedback.getRequestType(),
                feedback.getProvider(),
                feedback.getPromptSummary(),
                feedback.getSummary(),
                recommendations(feedback.getRecommendations()),
                feedback.getFeedback(),
                DISCLAIMER,
                feedback.getCreatedAt()
        );
    }

    private String recommendationsJson(List<String> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) {
            return "";
        }
        return String.join("\n", recommendations);
    }

    private List<String> recommendations(String recommendationsJson) {
        if (recommendationsJson == null || recommendationsJson.isBlank()) {
            return List.of();
        }
        return Arrays.stream(recommendationsJson.split("\\R"))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private HttpStatus providerStatus(AiProviderException exception) {
        if ("AI_PROVIDER_NOT_CONFIGURED".equals(exception.errorCode())) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        return HttpStatus.BAD_GATEWAY;
    }

    private String userSafeProviderMessage(AiProviderException exception) {
        if ("AI_PROVIDER_NOT_CONFIGURED".equals(exception.errorCode())) {
            return "AI feedback is not configured.";
        }
        return "AI feedback is temporarily unavailable.";
    }

    private String normalizeRequestType(String value) {
        return cleanRequired(value, "general").toLowerCase(Locale.ROOT);
    }

    private String cleanRequired(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }
}
