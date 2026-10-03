package com.jakeer.authservice.service;

import com.jakeer.authservice.dto.LoginRequest;
import com.jakeer.authservice.dto.LoginResponse;
import com.jakeer.authservice.entity.AuthUser;
import com.jakeer.authservice.repository.AuthRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(
            AuthRepository authRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        // 1. Find user by email
        AuthUser user = authRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password"));

        // 2. Check account status
        if (!user.isEnabled()) {
            throw new RuntimeException("User account is disabled");
        }

        // 3. Enforce any active temporary lockout
        LocalDateTime now = LocalDateTime.now();
        if (user.getLockedUntil() != null) {
            if (user.getLockedUntil().isAfter(now)) {
                throw new RuntimeException("Invalid email or password");
            }

            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
            authRepository.save(user);
        }

        // 4. Verify password
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
            }
            authRepository.save(user);

            throw new RuntimeException("Invalid email or password");
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        authRepository.save(user);

        // 5. Generate JWT token
        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole()
        );

        // 6. Prepare login response
        return new LoginResponse(
                user.getEmail(),
                user.getRole(),
                token
        );
    }

    @Override
    public void createAuthUser(String email, String password) {

        if (authRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        AuthUser user = new AuthUser();

        // Set email
        user.setEmail(email);

        // Encode password before saving
        user.setPassword(
                passwordEncoder.encode(password)
        );

        // Default role
        user.setRole("USER");

        // Enable account by default
        user.setEnabled(true);

        // Save user into database
        authRepository.save(user);
    }
}