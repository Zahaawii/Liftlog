package com.liftlogai.auth.service;

import java.time.Instant;

public record TokenPayload(
        String type,
        Long userId,
        Instant issuedAt,
        Instant expiresAt,
        String tokenId
) {
}
