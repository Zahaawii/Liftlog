package com.liftlogai.nutrition.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyNutritionSummaryResponse(
        LocalDate date,
        int calories,
        BigDecimal protein,
        BigDecimal carbohydrates,
        BigDecimal fat
) {
}
