package com.cvflow.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth.jwt")
public record JwtProperties(String secret, long expirationHours) {
    public JwtProperties {
        if (secret == null || secret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.auth.jwt.secret must be at least 32 bytes (set JWT_SECRET)");
        }
        if (expirationHours <= 0) {
            throw new IllegalStateException("app.auth.jwt.expiration-hours must be positive");
        }
    }
}
