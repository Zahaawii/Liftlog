package com.liftlogai.auth.service;

import com.liftlogai.config.AuthProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final AuthProperties properties;
    private final Clock clock;

    public TokenService(AuthProperties properties) {
        this.properties = properties;
        this.clock = Clock.systemUTC();
    }

    public String createAccessToken(Long userId) {
        Instant now = Instant.now(clock);
        return create("access", userId, now, now.plus(properties.accessTokenMinutes(), ChronoUnit.MINUTES));
    }

    public String createRefreshToken(Long userId) {
        Instant now = Instant.now(clock);
        return create("refresh", userId, now, now.plus(properties.refreshTokenDays(), ChronoUnit.DAYS));
    }

    public Optional<TokenPayload> validate(String token, String expectedType) {
        String[] parts = token.split("\\.", 2);
        if (parts.length != 2) {
            return Optional.empty();
        }

        String payload = decode(parts[0]);
        byte[] expectedSignature = sign(payload);
        byte[] actualSignature = decodeBytes(parts[1]);
        if (!MessageDigest.isEqual(expectedSignature, actualSignature)) {
            return Optional.empty();
        }

        String[] values = payload.split("\\|");
        if (values.length != 6 || !properties.issuer().equals(values[0]) || !expectedType.equals(values[1])) {
            return Optional.empty();
        }

        try {
            TokenPayload tokenPayload = new TokenPayload(
                    values[1],
                    Long.parseLong(values[2]),
                    Instant.ofEpochSecond(Long.parseLong(values[3])),
                    Instant.ofEpochSecond(Long.parseLong(values[4])),
                    values[5]
            );
            if (tokenPayload.expiresAt().isBefore(Instant.now(clock))) {
                return Optional.empty();
            }
            return Optional.of(tokenPayload);
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    public String hashToken(String token) {
        byte[] digest = sha256(token.getBytes(StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder(digest.length * 2);
        for (byte value : digest) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }

    private String create(String type, Long userId, Instant issuedAt, Instant expiresAt) {
        String payload = String.join(
                "|",
                properties.issuer(),
                type,
                userId.toString(),
                Long.toString(issuedAt.getEpochSecond()),
                Long.toString(expiresAt.getEpochSecond()),
                UUID.randomUUID().toString()
        );
        return encode(payload.getBytes(StandardCharsets.UTF_8)) + "." + encode(sign(payload));
    }

    private byte[] sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(properties.tokenSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not sign authentication token.", exception);
        }
    }

    private byte[] sha256(byte[] value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not hash token.", exception);
        }
    }

    private String decode(String value) {
        return new String(decodeBytes(value), StandardCharsets.UTF_8);
    }

    private byte[] decodeBytes(String value) {
        try {
            return DECODER.decode(value);
        } catch (RuntimeException exception) {
            return new byte[0];
        }
    }

    private String encode(byte[] value) {
        return ENCODER.encodeToString(value);
    }
}
