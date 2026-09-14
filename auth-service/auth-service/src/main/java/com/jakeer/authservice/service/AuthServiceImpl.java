package com.jakeer.authservice.service;

import com.jakeer.authservice.dto.LoginRequest;
import com.jakeer.authservice.dto.LoginResponse;
import com.jakeer.authservice.entity.AuthUser;
import com.jakeer.authservice.repository.AuthRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
        Optional<AuthUser> userOptional =
                authRepository.findByEmail(request.getEmail());

        // 2. User not found
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }

        AuthUser user = userOptional.get();

        // 3. Check account status
        if (!user.isEnabled()) {
            throw new RuntimeException("User account is disabled");
        }

        // 4. Verify password
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Invalid email or password");
        }

        // 5. Generate JWT
        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole()
        );

        // 6. Prepare response
        return new LoginResponse(
                user.getEmail(),
                user.getRole(),
                token
        );
    }


    public void createAuthUser(String email, String password) {

        AuthUser user = new AuthUser();

        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");
        user.setEnabled(true);

        authRepository.save(user);
    }
}