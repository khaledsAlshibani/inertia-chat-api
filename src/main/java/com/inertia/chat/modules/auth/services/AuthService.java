package com.inertia.chat.modules.auth.services;

import com.inertia.chat.modules.auth.dto.*;

public interface AuthService {
    AuthResponse signup(SignupRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(String refreshToken);
    void logout(String refreshToken);
}