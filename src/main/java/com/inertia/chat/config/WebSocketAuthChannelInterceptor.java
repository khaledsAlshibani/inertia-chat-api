package com.inertia.chat.config;

import com.inertia.chat.modules.users.enums.UserStatus;
import com.inertia.chat.modules.auth.utils.JWTUtil;
import com.inertia.chat.modules.users.entities.User;
import com.inertia.chat.modules.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.info("Processing WebSocket CONNECT request");
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    log.info("Validating JWT token");
                    String email = jwtUtil.extractEmail(token);
                    if (email != null) {
                        log.info("Found email in token: {}", email);
                        User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> {
                                    log.error("User not found for email: {}", email);
                                    return new RuntimeException("User not found");
                                });

                        // Set user status to ONLINE
                        user.setStatus(UserStatus.ONLINE);
                        userRepository.save(user);
                        log.info("User found: {}", user.getUsername());
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                user, null, user.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        accessor.setUser(auth);
                        log.info("WebSocket authentication successful for user: {}", user.getUsername());
                    }
                } catch (Exception e) {
                    log.error("WebSocket authentication failed: {}", e.getMessage(), e);
                    return null;
                }
            } else {
                log.error("No valid Authorization header found in WebSocket CONNECT request");
                return null;
            }
        }
        return message;
    }
}