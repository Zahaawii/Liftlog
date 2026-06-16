package com.liftlogai.goal.service;

import com.liftlogai.auth.security.AuthenticatedUser;
import com.liftlogai.common.dto.PagedResponse;
import com.liftlogai.common.error.AppException;
import com.liftlogai.goal.dto.GoalCheckInRequest;
import com.liftlogai.goal.dto.GoalCheckInResponse;
import com.liftlogai.goal.dto.GoalRequest;
import com.liftlogai.goal.dto.GoalResponse;
import com.liftlogai.goal.entity.Goal;
import com.liftlogai.goal.entity.GoalCheckIn;
import com.liftlogai.goal.entity.GoalStatus;
import com.liftlogai.goal.repository.GoalCheckInRepository;
import com.liftlogai.goal.repository.GoalRepository;
import com.liftlogai.nutrition.repository.NutritionLogRepository;
import com.liftlogai.user.entity.User;
import com.liftlogai.user.repository.UserRepository;
import com.liftlogai.workout.repository.WorkoutRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class GoalService {

    private static final Logger log = LoggerFactory.getLogger(GoalService.class);
    private static final int MAX_PAGE_SIZE = 100;

    private final GoalRepository goalRepository;
    private final GoalCheckInRepository goalCheckInRepository;
    private final UserRepository userRepository;
    private final WorkoutRepository workoutRepository;
    private final NutritionLogRepository nutritionLogRepository;

    public GoalService(
            GoalRepository goalRepository,
            GoalCheckInRepository goalCheckInRepository,
            UserRepository userRepository,
            WorkoutRepository workoutRepository,
            NutritionLogRepository nutritionLogRepository
    ) {
        this.goalRepository = goalRepository;
        this.goalCheckInRepository = goalCheckInRepository;
        this.userRepository = userRepository;
        this.workoutRepository = workoutRepository;
        this.nutritionLogRepository = nutritionLogRepository;
    }

    @Transactional
    public GoalResponse createGoal(AuthenticatedUser authenticatedUser, GoalRequest request) {
        validateDates(request.startDate(), request.targetDate());
        User user = findUser(authenticatedUser);
        Goal goal = new Goal(
                user,
                normalize(request.goalType()),
                cleanRequired(request.title()),
                normalize(request.targetMetric()),
                request.targetValue(),
                request.currentBaseline(),
                request.startDate(),
                request.targetDate(),
                request.status() == null ? GoalStatus.ACTIVE : request.status()
        );

        Goal saved = goalRepository.save(goal);
        log.info("Goal created userId={} goalId={}", authenticatedUser.id(), saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<GoalResponse> listGoals(AuthenticatedUser authenticatedUser, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return PagedResponse.from(goalRepository.findByUserId(authenticatedUser.id(), pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public List<GoalResponse> activeGoals(AuthenticatedUser authenticatedUser) {
        return goalRepository.findByUserIdAndStatusOrderByCreatedAtDesc(authenticatedUser.id(), GoalStatus.ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GoalResponse getGoal(AuthenticatedUser authenticatedUser, Long goalId) {
        return toResponse(findOwnedGoal(authenticatedUser.id(), goalId));
    }

    @Transactional
    public GoalResponse updateGoal(AuthenticatedUser authenticatedUser, Long goalId, GoalRequest request) {
        validateDates(request.startDate(), request.targetDate());
        Goal goal = findOwnedGoal(authenticatedUser.id(), goalId);
        goal.update(
                normalize(request.goalType()),
                cleanRequired(request.title()),
                normalize(request.targetMetric()),
                request.targetValue(),
                request.currentBaseline(),
                request.startDate(),
                request.targetDate(),
                request.status() == null ? goal.getStatus() : request.status()
        );

        Goal saved = goalRepository.save(goal);
        log.info("Goal updated userId={} goalId={}", authenticatedUser.id(), saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public void deleteGoal(AuthenticatedUser authenticatedUser, Long goalId) {
        Goal goal = findOwnedGoal(authenticatedUser.id(), goalId);
        goalRepository.delete(goal);
        log.info("Goal deleted userId={} goalId={}", authenticatedUser.id(), goalId);
    }

    @Transactional
    public GoalCheckInResponse createCheckIn(
            AuthenticatedUser authenticatedUser,
            Long goalId,
            GoalCheckInRequest request
    ) {
        Goal goal = findOwnedGoal(authenticatedUser.id(), goalId);
        GoalCheckIn checkIn = goalCheckInRepository.save(new GoalCheckIn(
                goal,
                request.checkInDate(),
                request.value(),
                clean(request.notes())
        ));
        log.info("Goal check-in created userId={} goalId={} checkInId={}", authenticatedUser.id(), goalId, checkIn.getId());
        return toCheckInResponse(checkIn);
    }

    @Transactional(readOnly = true)
    public List<GoalCheckInResponse> listCheckIns(AuthenticatedUser authenticatedUser, Long goalId) {
        Goal goal = findOwnedGoal(authenticatedUser.id(), goalId);
        return goalCheckInRepository.findByGoalIdOrderByCheckInDateDescCreatedAtDesc(goal.getId())
                .stream()
                .map(this::toCheckInResponse)
                .toList();
    }

    private GoalResponse toResponse(Goal goal) {
        BigDecimal currentValue = currentValue(goal);
        return new GoalResponse(
                goal.getId(),
                goal.getGoalType(),
                goal.getTitle(),
                goal.getTargetMetric(),
                goal.getTargetValue(),
                currentValue,
                progressPercent(currentValue, goal.getTargetValue()),
                goal.getStatus(),
                goal.getStartDate(),
                goal.getTargetDate(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }

    private BigDecimal currentValue(Goal goal) {
        return switch (goal.getTargetMetric()) {
            case "weekly_workout_count", "workout_count" -> BigDecimal.valueOf(workoutCount(goal));
            case "daily_calories" -> dailyCalories(goal);
            default -> latestCheckInValue(goal);
        };
    }

    private long workoutCount(Goal goal) {
        LocalDate endDate = goal.getTargetDate() == null ? LocalDate.now() : goal.getTargetDate();
        if (endDate.isBefore(goal.getStartDate())) {
            return 0;
        }
        return workoutRepository.countByUserIdAndWorkoutDateBetween(
                goal.getUser().getId(),
                goal.getStartDate(),
                endDate
        );
    }

    private BigDecimal dailyCalories(Goal goal) {
        List<Object[]> rows = nutritionLogRepository.dailyTotals(goal.getUser().getId(), goal.getStartDate());
        if (rows.isEmpty() || rows.getFirst()[0] == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(((Number) rows.getFirst()[0]).longValue());
    }

    private BigDecimal latestCheckInValue(Goal goal) {
        return goalCheckInRepository.findFirstByGoalIdOrderByCheckInDateDescCreatedAtDesc(goal.getId())
                .map(GoalCheckIn::getValue)
                .orElse(goal.getCurrentBaseline() == null ? BigDecimal.ZERO : goal.getCurrentBaseline());
    }

    private int progressPercent(BigDecimal currentValue, BigDecimal targetValue) {
        if (targetValue.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        BigDecimal percent = currentValue
                .multiply(BigDecimal.valueOf(100))
                .divide(targetValue, 0, RoundingMode.HALF_UP);
        return Math.min(100, Math.max(0, percent.intValue()));
    }

    private GoalCheckInResponse toCheckInResponse(GoalCheckIn checkIn) {
        return new GoalCheckInResponse(
                checkIn.getId(),
                checkIn.getCheckInDate(),
                checkIn.getValue(),
                checkIn.getNotes(),
                checkIn.getCreatedAt()
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

    private Goal findOwnedGoal(Long userId, Long goalId) {
        return goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND,
                        "GOAL_NOT_FOUND",
                        "Goal was not found."
                ));
    }

    private void validateDates(LocalDate startDate, LocalDate targetDate) {
        if (targetDate != null && targetDate.isBefore(startDate)) {
            throw new AppException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "GOAL_DATE_RANGE_INVALID",
                    "Target date cannot be before start date."
            );
        }
    }

    private String normalize(String value) {
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
}
