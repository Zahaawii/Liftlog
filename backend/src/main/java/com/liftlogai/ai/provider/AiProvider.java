package com.liftlogai.ai.provider;

public interface AiProvider {

    AiProviderResponse generateFeedback(AiProviderRequest request);

    String providerName();
}
