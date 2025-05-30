package com.inertia.chat.modules.auth.controllers;

import com.inertia.chat.modules.auth.dto.*;
import com.inertia.chat.modules.auth.services.AuthService;
import com.inertia.chat.modules.auth.services.impl.AuthServiceImpl;
import com.inertia.chat.modules.auth.util.CookieUtil;
import com.inertia.chat.common.dto.EnvelopeResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request, HttpServletResponse response) {
        try {
            AuthResponse result = authService.signup(request);
            if (result instanceof AuthServiceImpl.AuthResponseWithRefresh authWithRefresh) {
                CookieUtil.setRefreshTokenCookie(response, authWithRefresh.getRefreshToken());
                return ResponseEntity.ok(EnvelopeResponse.success(new AuthResponse(authWithRefresh.getAccessToken(), authWithRefresh.getUsername(), authWithRefresh.getEmail()), "Signup successful"));
            }
            return ResponseEntity.ok(EnvelopeResponse.success(result, "Signup successful"));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(EnvelopeResponse.error(Collections.singletonList(e.getMessage())));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            AuthResponse result = authService.login(request);
            if (result instanceof AuthServiceImpl.AuthResponseWithRefresh authWithRefresh) {
                CookieUtil.setRefreshTokenCookie(response, authWithRefresh.getRefreshToken());
                return ResponseEntity.ok(EnvelopeResponse.success(new AuthResponse(authWithRefresh.getAccessToken(), authWithRefresh.getUsername(), authWithRefresh.getEmail()), "Login successful"));
            }
            return ResponseEntity.ok(EnvelopeResponse.success(result, "Login successful"));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(403)
                    .body(EnvelopeResponse.error(Collections.singletonList(e.getMessage())));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(403).body(EnvelopeResponse.error(Collections.singletonList("Refresh token cookie is missing")));
        }
        try {
            AuthResponse result = authService.refresh(refreshToken);
            if (result instanceof AuthServiceImpl.AuthResponseWithRefresh authWithRefresh) {
                CookieUtil.setRefreshTokenCookie(response, authWithRefresh.getRefreshToken());
                return ResponseEntity.ok(EnvelopeResponse.success(new AuthResponse(authWithRefresh.getAccessToken(), authWithRefresh.getUsername(), authWithRefresh.getEmail()), "Token refreshed successfully"));
            }
            return ResponseEntity.ok(EnvelopeResponse.success(result, "Token refreshed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(403)
                    .body(EnvelopeResponse.error(Collections.singletonList(e.getMessage())));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(value = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(403).body(EnvelopeResponse.error(Collections.singletonList("Refresh token cookie is missing")));
        }
        try {
            authService.logout(refreshToken);
            CookieUtil.clearRefreshTokenCookie(response);
            return ResponseEntity.ok(EnvelopeResponse.success(null, "Logged out successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(403)
                    .body(EnvelopeResponse.error(Collections.singletonList(e.getMessage())));
        }
    }
}