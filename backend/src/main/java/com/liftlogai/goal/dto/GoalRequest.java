package com.liftlogai.goal.dto;

import com.liftlogai.goal.entity.GoalStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalRequest(
        @NotBlank @Size(max = 40) String goalType,
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 80) String targetMetric,
        @NotNull @DecimalMin(value = "0.01") BigDecimal targetValue,
        @DecimalMin("0.0") BigDecimal currentBaseline,
        @NotNull LocalDate startDate,
        LocalDate targetDate,
        GoalStatus status
) {
}
