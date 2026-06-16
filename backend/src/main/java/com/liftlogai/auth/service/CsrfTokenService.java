package com.liftlogai.auth.service;

import com.liftlogai.config.AuthProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class CsrfTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final SecureRandom secureRandom = new SecureRandom();
    private final AuthProperties properties;

    public CsrfTokenService(AuthProperties properties) {
        this.properties = properties;
    }

    public String createToken() {
        byte[] nonce = new byte[32];
        secureRandom.nextBytes(nonce);
        String encodedNonce = ENCODER.encodeToString(nonce);
        return encodedNonce + "." + ENCODER.encodeToString(sign(encodedNonce));
    }

    public boolean isValid(String token) {
        String[] parts = token.split("\\.", 2);
        if (parts.length != 2) {
            return false;
        }
        byte[] expected = sign(parts[0]);
        byte[] actual;
        try {
            actual = DECODER.decode(parts[1]);
        } catch (RuntimeException exception) {
            return false;
        }
        return MessageDigest.isEqual(expected, actual);
    }

    private byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(properties.tokenSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not sign CSRF token.", exception);
        }
    }
}
