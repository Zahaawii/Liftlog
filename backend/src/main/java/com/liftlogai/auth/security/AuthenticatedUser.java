package com.liftlogai.auth.security;

public record AuthenticatedUser(
        Long id,
        String email
) {
}
