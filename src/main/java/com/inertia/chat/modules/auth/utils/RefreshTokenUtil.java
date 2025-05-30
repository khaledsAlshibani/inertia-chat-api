package com.inertia.chat.modules.auth.utils;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RefreshTokenUtil {
    
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public boolean isValidRefreshToken(String token) {
        try {
            UUID.fromString(token);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}