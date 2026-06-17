package com.liftlogai.ai.provider;

import java.util.List;

public class StubAiProvider implements AiProvider {

    @Override
    public AiProviderResponse generateFeedback(AiProviderRequest request) {
        return new AiProviderResponse(
                "Your recent fitness data has enough structure for a practical review.",
                List.of(
                        "Keep workout consistency stable before adding more volume.",
                        "Review nutrition totals alongside active goals before changing targets."
                ),
                "This is stubbed advisory feedback for local and test environments. It uses the same application flow as a real provider without making an external AI request."
        );
    }

    @Override
    public String providerName() {
        return "stub";
    }
}
