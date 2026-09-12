package com.cvflow.auth.service;

import com.cvflow.auth.config.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

/**
 * Minimal HS256 JWT implementation using JDK crypto. Avoids pulling Jackson 2 onto the
 * classpath (the project uses Jackson 3, which io.jsonwebtoken does not support).
 */
@Component
public class JwtService {
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    public String issue(UUID userId, String email) {
        Instant now = Instant.now();
        long issuedAt = now.getEpochSecond();
        long expiresAt = now.plusSeconds(properties.expirationHours() * 3600).getEpochSecond();
        String header = base64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = base64("{\"sub\":\"" + userId + "\",\"email\":\"" + escape(email) + "\",\"iat\":"
                + issuedAt + ",\"exp\":" + expiresAt + "}");
        String signature = sign(header + "." + payload);
        return header + "." + payload + "." + signature;
    }

    public Optional<UUID> validate(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return Optional.empty();
            }
            String signingInput = parts[0] + "." + parts[1];
            byte[] expected = DECODER.decode(sign(signingInput));
            byte[] actual = DECODER.decode(parts[2]);
            if (!MessageDigest.isEqual(expected, actual)) {
                return Optional.empty();
            }
            String payload = new String(DECODER.decode(parts[1]), StandardCharsets.UTF_8);
            Long exp = numberField(payload, "\"exp\":");
            String sub = stringField(payload, "\"sub\":\"");
            if (exp == null || sub == null || Instant.now().getEpochSecond() >= exp) {
                return Optional.empty();
            }
            return Optional.of(UUID.fromString(sub));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    private String sign(String input) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    properties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return ENCODER.encodeToString(mac.doFinal(input.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException | java.security.InvalidKeyException ex) {
            throw new IllegalStateException("JWT signing failed", ex);
        }
    }

    private String base64(String value) {
        return ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private Long numberField(String payload, String key) {
        int start = payload.indexOf(key);
        if (start < 0) {
            return null;
        }
        int end = start + key.length();
        int stop = end;
        while (stop < payload.length() && Character.isDigit(payload.charAt(stop))) {
            stop++;
        }
        try {
            return Long.parseLong(payload.substring(end, stop));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String stringField(String payload, String key) {
        int start = payload.indexOf(key);
        if (start < 0) {
            return null;
        }
        int end = start + key.length();
        int stop = payload.indexOf('"', end);
        if (stop < 0) {
            return null;
        }
        return payload.substring(end, stop);
    }
}
