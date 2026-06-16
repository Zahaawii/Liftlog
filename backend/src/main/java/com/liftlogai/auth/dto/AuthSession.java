package com.liftlogai.auth.dto;

import com.liftlogai.user.dto.UserResponse;

public record AuthSession(
        UserResponse user,
        String accessToken,
        String refreshToken,
        String csrfToken
) {
}
