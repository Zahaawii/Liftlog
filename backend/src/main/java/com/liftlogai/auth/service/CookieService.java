package com.liftlogai.auth.service;

import com.liftlogai.config.AuthProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    private static final String API_PATH = "/api";

    private final AuthProperties properties;

    public CookieService(AuthProperties properties) {
        this.properties = properties;
    }

    public void addAuthCookies(HttpServletResponse response, String accessToken, String refreshToken, String csrfToken) {
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie(accessToken).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie(refreshToken).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, csrfCookie(csrfToken, maxAgeRefresh()).toString());
    }

    public void addAccessCookie(HttpServletResponse response, String accessToken, String refreshToken, String csrfToken) {
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie(accessToken).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie(refreshToken).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, csrfCookie(csrfToken, maxAgeRefresh()).toString());
    }

    public void addCsrfCookie(HttpServletResponse response, String csrfToken) {
        response.addHeader(HttpHeaders.SET_COOKIE, csrfCookie(csrfToken, maxAgeRefresh()).toString());
    }

    public void clearAuthCookies(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, expireCookie(properties.accessCookieName(), true).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, expireCookie(properties.refreshCookieName(), true).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, expireCookie(properties.csrfCookieName(), false).toString());
    }

    public Optional<String> readAccessToken(HttpServletRequest request) {
        return readCookie(request, properties.accessCookieName());
    }

    public Optional<String> readRefreshToken(HttpServletRequest request) {
        return readCookie(request, properties.refreshCookieName());
    }

    public Optional<String> readCsrfToken(HttpServletRequest request) {
        return readCookie(request, properties.csrfCookieName());
    }

    public String csrfHeaderName() {
        return properties.csrfHeaderName();
    }

    private ResponseCookie accessCookie(String token) {
        return baseCookie(properties.accessCookieName(), token, maxAgeAccess(), true);
    }

    private ResponseCookie refreshCookie(String token) {
        return baseCookie(properties.refreshCookieName(), token, maxAgeRefresh(), true);
    }

    private ResponseCookie csrfCookie(String token, Duration maxAge) {
        return baseCookie(properties.csrfCookieName(), token, maxAge, false);
    }

    private ResponseCookie expireCookie(String name, boolean httpOnly) {
        return baseCookie(name, "", Duration.ZERO, httpOnly);
    }

    private ResponseCookie baseCookie(String name, String value, Duration maxAge, boolean httpOnly) {
        return ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(properties.cookieSecure())
                .sameSite(properties.cookieSameSite())
                .path(API_PATH)
                .maxAge(maxAge)
                .build();
    }

    private Optional<String> readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    private Duration maxAgeAccess() {
        return Duration.ofMinutes(properties.accessTokenMinutes());
    }

    private Duration maxAgeRefresh() {
        return Duration.ofDays(properties.refreshTokenDays());
    }
}
