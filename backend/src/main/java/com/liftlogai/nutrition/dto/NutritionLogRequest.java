package com.liftlogai.nutrition.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record NutritionLogRequest(
        @NotNull LocalDate logDate,
        @NotBlank @Size(max = 40) String mealType,
        @NotBlank @Size(max = 160) String foodName,
        @DecimalMin("0.0") BigDecimal servingQuantity,
        @Min(0) Integer calories,
        @DecimalMin("0.0") BigDecimal protein,
        @DecimalMin("0.0") BigDecimal carbohydrates,
        @DecimalMin("0.0") BigDecimal fat,
        @Size(max = 1000) String notes
) {
}
