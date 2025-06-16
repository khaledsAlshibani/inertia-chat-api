package com.inertia.chat.modules.auth.services;

import com.inertia.chat.modules.auth.dto.*;
import com.inertia.chat.modules.users.entities.User;

public interface AuthService {
    AuthResponse signup(SignupRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(String refreshToken);
    void logout(String refreshToken);
    AuthResponse refreshTokensForUser(User user);
}