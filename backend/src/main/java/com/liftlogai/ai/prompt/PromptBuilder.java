package com.liftlogai.ai.prompt;

import com.liftlogai.ai.dto.AiFeedbackRequest;
import com.liftlogai.goal.dto.GoalResponse;
import com.liftlogai.workout.dto.WorkoutExerciseResponse;
import com.liftlogai.workout.dto.WorkoutResponse;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    private static final int MAX_QUESTION_LENGTH = 500;
    private static final Pattern SECRET_PATTERN = Pattern.compile(
            "(?i)(password|token|secret|api[-_ ]?key)\\s*[:=]?\\s*\\S+"
    );

    public PromptBuildResult build(AiFeedbackRequest request, PromptContext context) {
        boolean includeWorkouts = enabled(request.includeWorkouts());
        boolean includeNutrition = enabled(request.includeNutrition());
        boolean includeGoals = enabled(request.includeGoals());

        String prompt = """
                Provide concise, practical fitness feedback based on the bounded user data below.
                Treat the response as informational and not medical advice.
                Return JSON with fields: summary, recommendations, feedback.

                Request type: %s
                User question: %s

                Recent workouts:
                %s

                Today's nutrition:
                %s

                Active goals:
                %s
                """.formatted(
                cleanLabel(request.requestType()),
                safeQuestion(request.question()),
                includeWorkouts ? workoutLines(context.recentWorkouts()) : "Not included.",
                includeNutrition ? nutritionLine(context) : "Not included.",
                includeGoals ? goalLines(context.activeGoals()) : "Not included."
        );

        return new PromptBuildResult(prompt, promptSummary(request, includeWorkouts, includeNutrition, includeGoals));
    }

    private String promptSummary(
            AiFeedbackRequest request,
            boolean includeWorkouts,
            boolean includeNutrition,
            boolean includeGoals
    ) {
        String dataSources = String.join(", ", enabledSourceNames(includeWorkouts, includeNutrition, includeGoals));
        if (dataSources.isBlank()) {
            dataSources = "no fitness data sources";
        }
        String questionPart = request.question() == null || request.question().isBlank()
                ? "No custom question."
                : "Custom question included.";
        return "%s feedback using %s. %s".formatted(cleanLabel(request.requestType()), dataSources, questionPart);
    }

    private List<String> enabledSourceNames(boolean includeWorkouts, boolean includeNutrition, boolean includeGoals) {
        java.util.ArrayList<String> names = new java.util.ArrayList<>();
        if (includeWorkouts) {
            names.add("recent workouts");
        }
        if (includeNutrition) {
            names.add("nutrition totals");
        }
        if (includeGoals) {
            names.add("active goals");
        }
        return names;
    }

    private String workoutLines(List<WorkoutResponse> workouts) {
        if (workouts == null || workouts.isEmpty()) {
            return "No recent workouts.";
        }
        return workouts.stream()
                .limit(5)
                .map(this::workoutLine)
                .reduce((left, right) -> left + "\n" + right)
                .orElse("No recent workouts.");
    }

    private String workoutLine(WorkoutResponse workout) {
        String exercises = workout.exercises().stream()
                .limit(5)
                .map(WorkoutExerciseResponse::exercise)
                .map(exercise -> exercise.name())
                .map(this::cleanLabel)
                .reduce((left, right) -> left + ", " + right)
                .orElse("no exercises");
        return "- %s: %s (%d exercises)".formatted(
                workout.workoutDate(),
                cleanLabel(workout.title() == null ? "Workout" : workout.title()),
                workout.exercises().size()
        ) + " [" + exercises + "]";
    }

    private String nutritionLine(PromptContext context) {
        if (context.nutritionToday() == null) {
            return "No nutrition totals available.";
        }
        return "%s: %d calories, %sg protein, %sg carbs, %sg fat".formatted(
                context.nutritionToday().date(),
                context.nutritionToday().calories(),
                context.nutritionToday().protein(),
                context.nutritionToday().carbohydrates(),
                context.nutritionToday().fat()
        );
    }

    private String goalLines(List<GoalResponse> goals) {
        if (goals == null || goals.isEmpty()) {
            return "No active goals.";
        }
        return goals.stream()
                .limit(5)
                .map(this::goalLine)
                .reduce((left, right) -> left + "\n" + right)
                .orElse("No active goals.");
    }

    private String goalLine(GoalResponse goal) {
        return "- %s: %s of %s %s, %d%% complete".formatted(
                cleanLabel(goal.title()),
                goal.currentValue(),
                goal.targetValue(),
                cleanLabel(goal.targetMetric()),
                goal.progressPercent()
        );
    }

    private String safeQuestion(String question) {
        if (question == null || question.isBlank()) {
            return "No specific question.";
        }
        String redacted = SECRET_PATTERN.matcher(question.trim()).replaceAll("$1 [REDACTED]");
        if (redacted.length() <= MAX_QUESTION_LENGTH) {
            return redacted;
        }
        return redacted.substring(0, MAX_QUESTION_LENGTH);
    }

    private String cleanLabel(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private boolean enabled(Boolean value) {
        return value == null || value;
    }
}
