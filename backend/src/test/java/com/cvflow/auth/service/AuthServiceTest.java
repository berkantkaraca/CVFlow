package com.cvflow.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.cvflow.auth.config.JwtProperties;
import com.cvflow.auth.dto.LoginRequest;
import com.cvflow.auth.dto.RegisterRequest;
import com.cvflow.common.exception.EmailAlreadyExistsException;
import com.cvflow.common.exception.InvalidCredentialsException;
import com.cvflow.cv.entity.User;
import com.cvflow.cv.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AuthService service;

    @BeforeEach
    void setup() {
        service = new AuthService(
                userRepository,
                passwordEncoder,
                new JwtService(new JwtProperties("test-secret-at-least-32-bytes-long!!", 24)));
    }

    @Test
    void registerHashesPasswordAndReturnsToken() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var response = service.register(new RegisterRequest("User@Example.com", "password123"));

        assertNotNull(response.token());
        assertEquals("user@example.com", response.email());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(
                EmailAlreadyExistsException.class, () -> service.register(new RegisterRequest("user@example.com", "password123")));
    }

    @Test
    void loginSucceedsWithCorrectPassword() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        var response = service.login(new LoginRequest("user@example.com", "password123"));

        assertEquals(user.getId(), response.userId());
        assertTrue(response.token().split("\\.").length == 3);
    }

    @Test
    void loginRejectsWrongPasswordUnknownEmailAndPasswordlessUser() {
        User withoutPassword = new User();
        withoutPassword.setId(UUID.randomUUID());
        withoutPassword.setEmail("demo@cvsaas.local");
        when(userRepository.findByEmail("demo@cvsaas.local")).thenReturn(Optional.of(withoutPassword));

        assertThrows(
                InvalidCredentialsException.class,
                () -> service.login(new LoginRequest("demo@cvsaas.local", "whatever")));

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        assertThrows(
                InvalidCredentialsException.class,
                () -> service.login(new LoginRequest("user@example.com", "wrong")));

        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class, () -> service.login(new LoginRequest("ghost@example.com", "x")));
    }
}
