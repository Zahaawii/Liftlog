package com.liftlogai.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiFeedbackRequest(
        @NotBlank @Size(max = 80) String requestType,
        @Size(max = 1000) String question,
        Boolean includeWorkouts,
        Boolean includeNutrition,
        Boolean includeGoals
) {
}
