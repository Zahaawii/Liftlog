package com.liftlogai.nutrition.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record NutritionLogResponse(
        Long id,
        LocalDate logDate,
        String mealType,
        String foodName,
        BigDecimal servingQuantity,
        Integer calories,
        BigDecimal protein,
        BigDecimal carbohydrates,
        BigDecimal fat,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
