package com.liftlogai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "liftlog.auth")
public record AuthProperties(
        String issuer,
        String tokenSecret,
        long accessTokenMinutes,
        long refreshTokenDays,
        boolean cookieSecure,
        String cookieSameSite,
        String accessCookieName,
        String refreshCookieName,
        String csrfCookieName,
        String csrfHeaderName
) {
    public AuthProperties {
        if (tokenSecret == null || tokenSecret.length() < 32) {
            throw new IllegalArgumentException("Authentication token secret must be at least 32 characters.");
        }
    }
}
