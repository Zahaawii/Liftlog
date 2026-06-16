package com.liftlogai.goal.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record GoalCheckInResponse(
        Long id,
        LocalDate checkInDate,
        BigDecimal value,
        String notes,
        Instant createdAt
) {
}
