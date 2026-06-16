package com.liftlogai.dashboard.service;

import com.liftlogai.auth.security.AuthenticatedUser;
import com.liftlogai.common.dto.PagedResponse;
import com.liftlogai.dashboard.dto.DashboardSummaryResponse;
import com.liftlogai.goal.service.GoalService;
import com.liftlogai.nutrition.service.NutritionService;
import com.liftlogai.workout.dto.WorkoutResponse;
import com.liftlogai.workout.repository.WorkoutRepository;
import com.liftlogai.workout.service.WorkoutService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final WorkoutRepository workoutRepository;
    private final WorkoutService workoutService;
    private final NutritionService nutritionService;
    private final GoalService goalService;

    public DashboardService(
            WorkoutRepository workoutRepository,
            WorkoutService workoutService,
            NutritionService nutritionService,
            GoalService goalService
    ) {
        this.workoutRepository = workoutRepository;
        this.workoutService = workoutService;
        this.nutritionService = nutritionService;
        this.goalService = goalService;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary(AuthenticatedUser authenticatedUser) {
        LocalDate today = LocalDate.now();
        PagedResponse<WorkoutResponse> recentWorkouts = workoutService.listWorkouts(authenticatedUser, 0, 5);
        long weeklyWorkoutCount = workoutRepository.countByUserIdAndWorkoutDateBetween(
                authenticatedUser.id(),
                today.minusDays(6),
                today
        );

        return new DashboardSummaryResponse(
                weeklyWorkoutCount,
                recentWorkouts.items(),
                nutritionService.dailySummary(authenticatedUser, today),
                goalService.activeGoals(authenticatedUser),
                null
        );
    }
}
