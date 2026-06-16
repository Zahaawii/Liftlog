package com.liftlogai.goal.dto;

import com.liftlogai.goal.entity.GoalStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record GoalResponse(
        Long id,
        String goalType,
        String title,
        String targetMetric,
        BigDecimal targetValue,
        BigDecimal currentValue,
        int progressPercent,
        GoalStatus status,
        LocalDate startDate,
        LocalDate targetDate,
        Instant createdAt,
        Instant updatedAt
) {
}
