package com.liftlogai.auth.service;

import com.liftlogai.auth.dto.AuthSession;
import com.liftlogai.auth.dto.LoginRequest;
import com.liftlogai.auth.dto.RegisterRequest;
import com.liftlogai.auth.security.AuthenticatedUser;
import com.liftlogai.common.error.AppException;
import com.liftlogai.user.dto.UserResponse;
import com.liftlogai.user.entity.User;
import com.liftlogai.user.repository.UserRepository;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final CsrfTokenService csrfTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService,
            RefreshTokenService refreshTokenService,
            CsrfTokenService csrfTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
        this.csrfTokenService = csrfTokenService;
    }

    @Transactional
    public AuthSession register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new AppException(HttpStatus.CONFLICT, "AUTH_EMAIL_ALREADY_REGISTERED", "Email is already registered.");
        }

        User user = userRepository.save(new User(
                email,
                passwordEncoder.encode(request.password()),
                cleanDisplayName(request.displayName())
        ));
        log.info("User registered userId={}", user.getId());
        return issueSession(user);
    }

    @Transactional
    public AuthSession login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmail(email)
                .filter(foundUser -> passwordEncoder.matches(request.password(), foundUser.getPasswordHash()))
                .orElseThrow(() -> {
                    log.warn("Failed login attempt");
                    return new AppException(
                            HttpStatus.UNAUTHORIZED,
                            "AUTH_INVALID_CREDENTIALS",
                            "Invalid email or password."
                    );
                });

        user.markLoggedIn();
        log.info("User logged in userId={}", user.getId());
        return issueSession(user);
    }

    @Transactional
    public AuthSession refresh(String refreshToken) {
        tokenService.validate(refreshToken, "refresh")
                .orElseThrow(() -> new AppException(
                        HttpStatus.UNAUTHORIZED,
                        "REFRESH_TOKEN_INVALID",
                        "Refresh session is invalid."
                ));
        User user = refreshTokenService.consume(refreshToken);
        return issueSession(user);
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(AuthenticatedUser authenticatedUser) {
        User user = userRepository.findById(authenticatedUser.id())
                .orElseThrow(() -> new AppException(
                        HttpStatus.UNAUTHORIZED,
                        "AUTHENTICATION_REQUIRED",
                        "Authentication is required."
                ));
        return toResponse(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    public String createCsrfToken() {
        return csrfTokenService.createToken();
    }

    private AuthSession issueSession(User user) {
        String accessToken = tokenService.createAccessToken(user.getId());
        String refreshToken = tokenService.createRefreshToken(user.getId());
        refreshTokenService.store(user, refreshToken);
        return new AuthSession(toResponse(user), accessToken, refreshToken, csrfTokenService.createToken());
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String cleanDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return null;
        }
        return displayName.trim();
    }
}
