package com.liftlogai.dashboard.dto;

import com.liftlogai.goal.dto.GoalResponse;
import com.liftlogai.nutrition.dto.DailyNutritionSummaryResponse;
import com.liftlogai.workout.dto.WorkoutResponse;
import java.util.List;

public record DashboardSummaryResponse(
        long weeklyWorkoutCount,
        List<WorkoutResponse> recentWorkouts,
        DailyNutritionSummaryResponse nutritionToday,
        List<GoalResponse> activeGoals,
        Object latestAiFeedback
) {
}
