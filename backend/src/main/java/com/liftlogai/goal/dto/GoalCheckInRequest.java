package com.liftlogai.goal.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalCheckInRequest(
        @NotNull LocalDate checkInDate,
        @NotNull @DecimalMin("0.0") BigDecimal value,
        @Size(max = 1000) String notes
) {
}
