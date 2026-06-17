package com.liftlogai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "liftlog.ai")
public record AiProperties(
        String provider,
        OpenAi openai
) {
    public String providerName() {
        return provider == null || provider.isBlank() ? "openai" : provider.trim().toLowerCase();
    }

    public OpenAi openaiConfig() {
        return openai == null ? new OpenAi("", "gpt-4o-mini", "https://api.openai.com/v1/chat/completions", 30) : openai;
    }

    public record OpenAi(
            String apiKey,
            String model,
            String endpoint,
            long timeoutSeconds
    ) {
        public String modelName() {
            return model == null || model.isBlank() ? "gpt-4o-mini" : model.trim();
        }

        public String endpointUrl() {
            return endpoint == null || endpoint.isBlank()
                    ? "https://api.openai.com/v1/chat/completions"
                    : endpoint.trim();
        }

        public long timeoutSecondsOrDefault() {
            return timeoutSeconds <= 0 ? 30 : timeoutSeconds;
        }
    }
}
