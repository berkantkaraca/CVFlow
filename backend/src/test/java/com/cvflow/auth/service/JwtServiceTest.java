package com.cvflow.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cvflow.auth.config.JwtProperties;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private static final String SECRET = "test-secret-at-least-32-bytes-long!!";
    private final JwtService jwtService = new JwtService(new JwtProperties(SECRET, 24));

    @Test
    void issuesTokenThatValidatesToUserId() {
        UUID userId = UUID.randomUUID();

        String token = jwtService.issue(userId, "user@example.com");

        assertEquals(Optional.of(userId), jwtService.validate(token));
    }

    @Test
    void rejectsTamperedToken() {
        String token = jwtService.issue(UUID.randomUUID(), "user@example.com");
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertTrue(jwtService.validate(tampered).isEmpty());
    }

    @Test
    void rejectsGarbageAndOtherSecrets() {
        assertTrue(jwtService.validate("not-a-token").isEmpty());
        JwtService other = new JwtService(new JwtProperties("another-secret-32-bytes-minimum!", 24));
        String foreign = other.issue(UUID.randomUUID(), "user@example.com");

        assertTrue(jwtService.validate(foreign).isEmpty());
    }
}
