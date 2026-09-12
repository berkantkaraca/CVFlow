package com.cvflow.auth.service;

import com.cvflow.auth.dto.AuthResponse;
import com.cvflow.auth.dto.LoginRequest;
import com.cvflow.auth.dto.RegisterRequest;
import com.cvflow.common.exception.EmailAlreadyExistsException;
import com.cvflow.common.exception.InvalidCredentialsException;
import com.cvflow.cv.entity.User;
import com.cvflow.cv.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException();
        }
        User user = new User();
        user.setId(java.util.UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        User saved = userRepository.save(user);
        return new AuthResponse(jwtService.issue(saved.getId(), saved.getEmail()), saved.getId(), saved.getEmail());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return new AuthResponse(jwtService.issue(user.getId(), user.getEmail()), user.getId(), user.getEmail());
    }
}
