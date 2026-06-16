package com.liftlogai.nutrition.service;

import com.liftlogai.auth.security.AuthenticatedUser;
import com.liftlogai.common.dto.PagedResponse;
import com.liftlogai.common.error.AppException;
import com.liftlogai.nutrition.dto.DailyNutritionSummaryResponse;
import com.liftlogai.nutrition.dto.NutritionLogRequest;
import com.liftlogai.nutrition.dto.NutritionLogResponse;
import com.liftlogai.nutrition.entity.NutritionLog;
import com.liftlogai.nutrition.repository.NutritionLogRepository;
import com.liftlogai.user.entity.User;
import com.liftlogai.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
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
public class NutritionService {

    private static final Logger log = LoggerFactory.getLogger(NutritionService.class);
    private static final int MAX_PAGE_SIZE = 100;

    private final NutritionLogRepository nutritionLogRepository;
    private final UserRepository userRepository;

    public NutritionService(
            NutritionLogRepository nutritionLogRepository,
            UserRepository userRepository
    ) {
        this.nutritionLogRepository = nutritionLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public NutritionLogResponse createLog(AuthenticatedUser authenticatedUser, NutritionLogRequest request) {
        validateNutritionValues(request);
        User user = findUser(authenticatedUser);
        NutritionLog logEntry = new NutritionLog(
                user,
                request.logDate(),
                normalizeMealType(request.mealType()),
                cleanRequired(request.foodName()),
                request.servingQuantity(),
                request.calories(),
                request.protein(),
                request.carbohydrates(),
                request.fat(),
                clean(request.notes())
        );

        NutritionLog saved = nutritionLogRepository.save(logEntry);
        log.info("Nutrition log created userId={} nutritionLogId={}", authenticatedUser.id(), saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<NutritionLogResponse> listLogs(AuthenticatedUser authenticatedUser, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "logDate").and(Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return PagedResponse.from(nutritionLogRepository.findByUserId(authenticatedUser.id(), pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public NutritionLogResponse getLog(AuthenticatedUser authenticatedUser, Long nutritionLogId) {
        return toResponse(findOwnedLog(authenticatedUser.id(), nutritionLogId));
    }

    @Transactional
    public NutritionLogResponse updateLog(
            AuthenticatedUser authenticatedUser,
            Long nutritionLogId,
            NutritionLogRequest request
    ) {
        validateNutritionValues(request);
        NutritionLog logEntry = findOwnedLog(authenticatedUser.id(), nutritionLogId);
        logEntry.update(
                request.logDate(),
                normalizeMealType(request.mealType()),
                cleanRequired(request.foodName()),
                request.servingQuantity(),
                request.calories(),
                request.protein(),
                request.carbohydrates(),
                request.fat(),
                clean(request.notes())
        );

        NutritionLog saved = nutritionLogRepository.save(logEntry);
        log.info("Nutrition log updated userId={} nutritionLogId={}", authenticatedUser.id(), saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public void deleteLog(AuthenticatedUser authenticatedUser, Long nutritionLogId) {
        NutritionLog logEntry = findOwnedLog(authenticatedUser.id(), nutritionLogId);
        nutritionLogRepository.delete(logEntry);
        log.info("Nutrition log deleted userId={} nutritionLogId={}", authenticatedUser.id(), nutritionLogId);
    }

    @Transactional(readOnly = true)
    public DailyNutritionSummaryResponse dailySummary(AuthenticatedUser authenticatedUser, LocalDate date) {
        List<Object[]> rows = nutritionLogRepository.dailyTotals(authenticatedUser.id(), date);
        Object[] totals = rows.isEmpty() ? null : rows.getFirst();
        return new DailyNutritionSummaryResponse(
                date,
                totals == null || totals[0] == null ? 0 : ((Number) totals[0]).intValue(),
                zeroIfNull(toBigDecimal(totals == null ? null : totals[1])),
                zeroIfNull(toBigDecimal(totals == null ? null : totals[2])),
                zeroIfNull(toBigDecimal(totals == null ? null : totals[3]))
        );
    }

    private User findUser(AuthenticatedUser authenticatedUser) {
        return userRepository.findById(authenticatedUser.id())
                .orElseThrow(() -> new AppException(
                        HttpStatus.UNAUTHORIZED,
                        "AUTHENTICATION_REQUIRED",
                        "Authentication is required."
                ));
    }

    private NutritionLog findOwnedLog(Long userId, Long nutritionLogId) {
        return nutritionLogRepository.findByIdAndUserId(nutritionLogId, userId)
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND,
                        "NUTRITION_LOG_NOT_FOUND",
                        "Nutrition log was not found."
                ));
    }

    private void validateNutritionValues(NutritionLogRequest request) {
        if (request.calories() == null
                && request.protein() == null
                && request.carbohydrates() == null
                && request.fat() == null) {
            throw new AppException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "NUTRITION_VALUE_REQUIRED",
                    "Nutrition log requires calories or at least one macronutrient value."
            );
        }
    }

    private NutritionLogResponse toResponse(NutritionLog logEntry) {
        return new NutritionLogResponse(
                logEntry.getId(),
                logEntry.getLogDate(),
                logEntry.getMealType(),
                logEntry.getFoodName(),
                logEntry.getServingQuantity(),
                logEntry.getCalories(),
                logEntry.getProtein(),
                logEntry.getCarbohydrates(),
                logEntry.getFat(),
                logEntry.getNotes(),
                logEntry.getCreatedAt(),
                logEntry.getUpdatedAt()
        );
    }

    private String normalizeMealType(String value) {
        return cleanRequired(value).toLowerCase(Locale.ROOT);
    }

    private String cleanRequired(String value) {
        return value.trim();
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        return BigDecimal.valueOf(((Number) value).doubleValue());
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
