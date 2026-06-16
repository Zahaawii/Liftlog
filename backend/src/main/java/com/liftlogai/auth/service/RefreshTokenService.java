package com.liftlogai.auth.service;

import com.liftlogai.auth.entity.RefreshToken;
import com.liftlogai.auth.repository.RefreshTokenRepository;
import com.liftlogai.common.error.AppException;
import com.liftlogai.config.AuthProperties;
import com.liftlogai.user.entity.User;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenService tokenService;
    private final AuthProperties properties;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            TokenService tokenService,
            AuthProperties properties
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenService = tokenService;
        this.properties = properties;
    }

    @Transactional
    public void store(User user, String refreshToken) {
        Instant expiresAt = Instant.now().plus(properties.refreshTokenDays(), ChronoUnit.DAYS);
        refreshTokenRepository.save(new RefreshToken(user, tokenService.hashToken(refreshToken), expiresAt));
    }

    @Transactional
    public User consume(String refreshToken) {
        String tokenHash = tokenService.hashToken(refreshToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(tokenHash)
                .orElseThrow(() -> new AppException(
                        HttpStatus.UNAUTHORIZED,
                        "REFRESH_TOKEN_INVALID",
                        "Refresh session is invalid."
                ));

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            storedToken.revoke();
            throw new AppException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED", "Refresh session has expired.");
        }

        storedToken.revoke();
        return storedToken.getUser();
    }

    @Transactional
    public void revoke(String refreshToken) {
        refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(tokenService.hashToken(refreshToken))
                .ifPresent(token -> {
                    token.revoke();
                    refreshTokenRepository.save(token);
                });
    }
}
