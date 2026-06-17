package com.liftlogai.ai.provider;

public record AiProviderRequest(
        String prompt,
        String promptSummary
) {
}
