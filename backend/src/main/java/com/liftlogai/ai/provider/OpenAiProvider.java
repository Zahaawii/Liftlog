package com.liftlogai.ai.provider;

import com.liftlogai.config.AiProperties;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenAiProvider implements AiProvider {

    private static final Pattern CONTENT_PATTERN = Pattern.compile("\"content\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"", Pattern.DOTALL);
    private static final Pattern SUMMARY_PATTERN = Pattern.compile("\"summary\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"", Pattern.DOTALL);
    private static final Pattern FEEDBACK_PATTERN = Pattern.compile("\"feedback\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"", Pattern.DOTALL);
    private static final Pattern RECOMMENDATIONS_PATTERN = Pattern.compile("\"recommendations\"\\s*:\\s*\\[(.*?)]", Pattern.DOTALL);
    private static final Pattern STRING_PATTERN = Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"");

    private final AiProperties.OpenAi properties;
    private final HttpClient httpClient;

    public OpenAiProvider(AiProperties.OpenAi properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.timeoutSecondsOrDefault()))
                .build();
    }

    @Override
    public AiProviderResponse generateFeedback(AiProviderRequest request) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new AiProviderException("AI_PROVIDER_NOT_CONFIGURED", "AI provider is not configured.");
        }

        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(properties.endpointUrl()))
                    .timeout(Duration.ofSeconds(properties.timeoutSecondsOrDefault()))
                    .header("Authorization", "Bearer " + properties.apiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody(request.prompt())))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new AiProviderException("AI_PROVIDER_FAILURE", "AI provider request failed.");
            }

            return parseResponse(response.body());
        } catch (AiProviderException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AiProviderException("AI_PROVIDER_FAILURE", "AI provider request failed.", exception);
        }
    }

    @Override
    public String providerName() {
        return "openai";
    }

    private String requestBody(String prompt) {
        return """
                {
                  "model": "%s",
                  "messages": [
                    {
                      "role": "system",
                      "content": "You provide concise, advisory fitness feedback. Return only JSON with summary, recommendations, and feedback fields. Do not provide medical diagnosis."
                    },
                    {
                      "role": "user",
                      "content": "%s"
                    }
                  ],
                  "temperature": 0.3
                }
                """.formatted(escapeJson(properties.modelName()), escapeJson(prompt));
    }

    private AiProviderResponse parseResponse(String body) {
        Matcher matcher = CONTENT_PATTERN.matcher(body);
        if (!matcher.find()) {
            throw new AiProviderException("AI_PROVIDER_INVALID_RESPONSE", "AI provider returned an invalid response.");
        }
        String content = unescapeJson(matcher.group(1));
        if (content.isBlank()) {
            throw new AiProviderException("AI_PROVIDER_INVALID_RESPONSE", "AI provider returned an invalid response.");
        }
        return parseContent(content);
    }

    private AiProviderResponse parseContent(String content) {
        String summary = fieldValue(content, SUMMARY_PATTERN, "AI feedback generated.");
        String feedback = fieldValue(content, FEEDBACK_PATTERN, content);
        return new AiProviderResponse(summary, recommendationValues(content), feedback);
    }

    private String fieldValue(String content, Pattern pattern, String defaultValue) {
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            return defaultValue;
        }
        String value = unescapeJson(matcher.group(1));
        return value.isBlank() ? defaultValue : value;
    }

    private List<String> recommendationValues(String content) {
        Matcher recommendationsMatcher = RECOMMENDATIONS_PATTERN.matcher(content);
        if (!recommendationsMatcher.find()) {
            return List.of();
        }
        List<String> recommendations = new ArrayList<>();
        Matcher stringMatcher = STRING_PATTERN.matcher(recommendationsMatcher.group(1));
        while (stringMatcher.find()) {
            String recommendation = unescapeJson(stringMatcher.group(1));
            if (!recommendation.isBlank()) {
                recommendations.add(recommendation);
            }
        }
        return recommendations;
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String unescapeJson(String value) {
        StringBuilder builder = new StringBuilder();
        boolean escaping = false;
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (!escaping) {
                if (current == '\\') {
                    escaping = true;
                } else {
                    builder.append(current);
                }
                continue;
            }
            switch (current) {
                case 'n' -> builder.append('\n');
                case 'r' -> builder.append('\r');
                case 't' -> builder.append('\t');
                case '"', '\\', '/' -> builder.append(current);
                default -> builder.append(current);
            }
            escaping = false;
        }
        if (escaping) {
            builder.append('\\');
        }
        return builder.toString();
    }
}
