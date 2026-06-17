package com.liftlogai.config;

import com.liftlogai.ai.provider.AiProvider;
import com.liftlogai.ai.provider.OpenAiProvider;
import com.liftlogai.ai.provider.StubAiProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiConfig {

    @Bean
    AiProvider aiProvider(AiProperties properties) {
        return switch (properties.providerName()) {
            case "stub", "fake" -> new StubAiProvider();
            case "openai" -> new OpenAiProvider(properties.openaiConfig());
            default -> new StubAiProvider();
        };
    }
}
