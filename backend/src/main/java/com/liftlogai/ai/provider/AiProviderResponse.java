package com.liftlogai.ai.provider;

import java.util.List;

public record AiProviderResponse(
        String summary,
        List<String> recommendations,
        String feedback
) {
    public AiProviderResponse {
        recommendations = recommendations == null ? List.of() : List.copyOf(recommendations);
    }
}
