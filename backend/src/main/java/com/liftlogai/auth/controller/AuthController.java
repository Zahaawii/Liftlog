package com.liftlogai.auth.controller;

import com.liftlogai.auth.dto.AuthResponse;
import com.liftlogai.auth.dto.AuthSession;
import com.liftlogai.auth.dto.CsrfResponse;
import com.liftlogai.auth.dto.LoginRequest;
import com.liftlogai.auth.dto.RegisterRequest;
import com.liftlogai.auth.security.AuthenticatedUser;
import com.liftlogai.auth.service.AuthService;
import com.liftlogai.auth.service.CookieService;
import com.liftlogai.common.error.AppException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CookieService cookieService;

    public AuthController(AuthService authService, CookieService cookieService) {
        this.authService = authService;
        this.cookieService = cookieService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    AuthResponse register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {
        AuthSession session = authService.register(request);
        cookieService.addAuthCookies(response, session.accessToken(), session.refreshToken(), session.csrfToken());
        return new AuthResponse(session.user());
    }

    @PostMapping("/login")
    AuthResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthSession session = authService.login(request);
        cookieService.addAuthCookies(response, session.accessToken(), session.refreshToken(), session.csrfToken());
        return new AuthResponse(session.user());
    }

    @PostMapping("/refresh")
    AuthResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieService.readRefreshToken(request)
                .orElseThrow(() -> new AppException(
                        HttpStatus.UNAUTHORIZED,
                        "REFRESH_TOKEN_MISSING",
                        "Refresh session is missing."
                ));
        AuthSession session = authService.refresh(refreshToken);
        cookieService.addAccessCookie(response, session.accessToken(), session.refreshToken(), session.csrfToken());
        return new AuthResponse(session.user());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(HttpServletRequest request, HttpServletResponse response) {
        cookieService.readRefreshToken(request).ifPresent(authService::logout);
        cookieService.clearAuthCookies(response);
    }

    @GetMapping("/me")
    AuthResponse me(Authentication authentication) {
        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        return new AuthResponse(authService.currentUser(principal));
    }

    @GetMapping("/csrf")
    CsrfResponse csrf(HttpServletResponse response) {
        String csrfToken = authService.createCsrfToken();
        cookieService.addCsrfCookie(response, csrfToken);
        return new CsrfResponse(csrfToken);
    }
}
