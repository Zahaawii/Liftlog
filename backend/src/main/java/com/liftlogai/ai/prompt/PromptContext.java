package com.liftlogai.ai.prompt;

import com.liftlogai.goal.dto.GoalResponse;
import com.liftlogai.nutrition.dto.DailyNutritionSummaryResponse;
import com.liftlogai.workout.dto.WorkoutResponse;
import java.util.List;

public record PromptContext(
        List<WorkoutResponse> recentWorkouts,
        DailyNutritionSummaryResponse nutritionToday,
        List<GoalResponse> activeGoals
) {
}
