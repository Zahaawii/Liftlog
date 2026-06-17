package com.liftlogai.ai.dto;

import java.time.Instant;
import java.util.List;

public record AiFeedbackResponse(
        Long id,
        Long userId,
        String requestType,
        String provider,
        String promptSummary,
        String summary,
        List<String> recommendations,
        String feedback,
        String disclaimer,
        Instant createdAt
) {
    public AiFeedbackResponse {
        recommendations = recommendations == null ? List.of() : List.copyOf(recommendations);
    }
}
