package com.inertia.chat.modules.auth.services.impl;

import com.inertia.chat.modules.auth.dto.*;
import com.inertia.chat.modules.auth.entities.RefreshToken;
import com.inertia.chat.modules.auth.repositories.RefreshTokenRepository;
import com.inertia.chat.modules.auth.services.AuthService;
import com.inertia.chat.modules.auth.utils.JWTUtil;
import com.inertia.chat.modules.auth.utils.HashUtil;
import com.inertia.chat.modules.users.entities.User;
import com.inertia.chat.modules.users.enums.UserStatus;
import com.inertia.chat.modules.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail()) || userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Email or username already exists");
        }
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        user = userRepository.save(user);
        log.info("User signed up successfully");

        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getId());
        String refreshTokenRaw = jwtUtil.generateRefreshToken(user.getUsername(), user.getId());
        String refreshTokenHash = HashUtil.sha256(refreshTokenRaw);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenHash)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(refreshToken);

        return new AuthResponseWithRefresh(accessToken, user.getUsername(), user.getEmail(), refreshTokenRaw);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmailOrUsername());
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByUsername(request.getEmailOrUsername());
        }
        User user = userOpt.orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Delete any existing refresh tokens for this user
        refreshTokenRepository.deleteByUserId(user.getId());

        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getId());
        String refreshTokenRaw = jwtUtil.generateRefreshToken(user.getUsername(), user.getId());
        String refreshTokenHash = HashUtil.sha256(refreshTokenRaw);
        
        log.info("Generated refresh token (raw): {}", refreshTokenRaw);
        log.info("Generated refresh token (hash): {}", refreshTokenHash);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenHash)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(refreshToken);
        log.info("Saved refresh token for user {} in DB", user.getId());

        return new AuthResponseWithRefresh(accessToken, user.getUsername(), user.getEmail(), refreshTokenRaw);
    }

    @Override
    @Transactional
    public AuthResponse refresh(String refreshTokenRaw) {
        if (refreshTokenRaw == null || refreshTokenRaw.isBlank()) {
            throw new RuntimeException("Refresh token is required");
        }

        // Validate JWT signature and expiry
        String email;
        try {
            email = jwtUtil.extractEmail(refreshTokenRaw);
            if (!jwtUtil.validateRefreshToken(refreshTokenRaw, email)) {
                throw new RuntimeException("Invalid or expired refresh token");
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        // Find all refresh tokens and check each one
        for (RefreshToken token : refreshTokenRepository.findAll()) {
            if (HashUtil.sha256(refreshTokenRaw).equals(token.getToken())) {
                if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
                    refreshTokenRepository.delete(token);
                    throw new RuntimeException("Refresh token has expired");
                }

                User user = token.getUser();
                String newAccessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getId());
                String newRefreshTokenRaw = jwtUtil.generateRefreshToken(user.getUsername(), user.getId());
                String newRefreshTokenHash = HashUtil.sha256(newRefreshTokenRaw);
                
                // Update the existing token
                token.setToken(newRefreshTokenHash);
                token.setCreatedAt(LocalDateTime.now());
                token.setExpiresAt(LocalDateTime.now().plusDays(7));
                refreshTokenRepository.save(token);

                return new AuthResponseWithRefresh(newAccessToken, user.getUsername(), user.getEmail(), newRefreshTokenRaw);
            }
        }
        throw new RuntimeException("Invalid refresh token");
    }

    @Override
    @Transactional
    public void logout(String refreshTokenRaw) {
        if (refreshTokenRaw == null || refreshTokenRaw.isBlank()) {
            return;
        }

        for (RefreshToken token : refreshTokenRepository.findAll()) {
            if (HashUtil.sha256(refreshTokenRaw).equals(token.getToken())) {
                // Set user status to OFFLINE on logout
                if (token.getUser() != null) {
                    token.getUser().setStatus(UserStatus.OFFLINE);
                    userRepository.save(token.getUser());
                }
                refreshTokenRepository.delete(token);
                break;
            }
        }
    }

    public static class AuthResponseWithRefresh extends AuthResponse {
        private final String refreshToken;
        public AuthResponseWithRefresh(String accessToken, String username, String email, String refreshToken) {
            super(accessToken, username, email);
            this.refreshToken = refreshToken;
        }
        public String getRefreshToken() { return refreshToken; }
    }
}