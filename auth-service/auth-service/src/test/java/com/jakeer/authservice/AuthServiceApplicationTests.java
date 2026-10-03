package com.jakeer.authservice;

import com.jakeer.authservice.dto.LoginRequest;
import com.jakeer.authservice.dto.LoginResponse;
import com.jakeer.authservice.entity.AuthUser;
import com.jakeer.authservice.repository.AuthRepository;
import com.jakeer.authservice.service.AuthServiceImpl;
import com.jakeer.authservice.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class AuthServiceApplicationTests {

	@Test
	void contextLoads() {
	}


    @Test
    void toStringDoesNotExposePassword() {
        AuthUser user = new AuthUser();
        user.setPassword("test-password");

        assertFalse(user.toString().contains("test-password"));
    }

    @Test
    void successfulLoginResetsFailedLoginState() {
        AuthRepository authRepository = mock(AuthRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtService jwtService = mock(JwtService.class);

        AuthUser user = new AuthUser();
        user.setEmail("customer@example.com");
        user.setPassword("encoded-password");
        user.setRole("USER");
        user.setEnabled(true);
        user.setFailedLoginAttempts(3);
        user.setLockedUntil(LocalDateTime.now().minusMinutes(1));

        when(authRepository.findByEmail("customer@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", "encoded-password"))
                .thenReturn(true);
        when(jwtService.generateToken("customer@example.com", "USER"))
                .thenReturn("test-token");

        LoginRequest request = new LoginRequest();
        request.setEmail("customer@example.com");
        request.setPassword("correct-password");

        AuthServiceImpl authService =
                new AuthServiceImpl(authRepository, passwordEncoder, jwtService);

        LoginResponse response = authService.login(request);

        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
        verify(authRepository, atLeastOnce()).save(user);
        assertEquals("customer@example.com", response.getEmail());
        assertEquals("USER", response.getRole());
        assertEquals("test-token", response.getToken());
    }

    @Test
    void failedLoginLocksAccountAfterFifthAttempt() {
        AuthRepository authRepository = mock(AuthRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtService jwtService = mock(JwtService.class);

        AuthUser user = new AuthUser();
        user.setEmail("customer@example.com");
        user.setPassword("encoded-password");
        user.setRole("USER");
        user.setEnabled(true);
        user.setFailedLoginAttempts(4);
        user.setLockedUntil(null);

        when(authRepository.findByEmail("customer@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("incorrect-password", "encoded-password"))
                .thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.setEmail("customer@example.com");
        request.setPassword("incorrect-password");

        AuthServiceImpl authService =
                new AuthServiceImpl(authRepository, passwordEncoder, jwtService);

        LocalDateTime beforeLogin = LocalDateTime.now();

        RuntimeException exception =
                assertThrows(RuntimeException.class, () -> authService.login(request));

        LocalDateTime afterLogin = LocalDateTime.now();

        assertEquals("Invalid email or password", exception.getMessage());
        assertEquals(5, user.getFailedLoginAttempts());
        assertNotNull(user.getLockedUntil());
        assertFalse(user.getLockedUntil().isBefore(beforeLogin.plusMinutes(15)));
        assertFalse(user.getLockedUntil().isAfter(afterLogin.plusMinutes(15)));
        verify(authRepository).save(user);
    }
}
