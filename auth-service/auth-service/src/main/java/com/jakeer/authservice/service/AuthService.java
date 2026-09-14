package com.jakeer.authservice.service;

import com.jakeer.authservice.dto.LoginRequest;
import com.jakeer.authservice.dto.LoginResponse;

public interface AuthService {

  LoginResponse login(LoginRequest request);

    void createAuthUser(String email, String password);

}
