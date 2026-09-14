package com.jakeer.authservice.rest;

import com.jakeer.authservice.dto.AuthRegisterRequest;
import com.jakeer.authservice.dto.LoginRequest;
import com.jakeer.authservice.dto.LoginResponse;
import com.jakeer.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @RequestBody AuthRegisterRequest request) {

        authService.createAuthUser(
                request.getEmail(),
                request.getPassword()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }



}