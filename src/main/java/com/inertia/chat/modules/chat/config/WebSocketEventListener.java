package com.inertia.chat.modules.chat.config;

import com.inertia.chat.modules.users.enums.UserStatus;
import com.inertia.chat.modules.chat.dto.ChatMessageDTO;
import com.inertia.chat.modules.chat.enums.MessageType;
import com.inertia.chat.modules.users.entities.User;
import com.inertia.chat.modules.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final UserRepository userRepository;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

        if (sessionAttributes != null) {
            if (sessionAttributes.get("gracefulExit") != null) {
                log.info("Graceful exit - no disconnect notification needed");
                return;
            }

            String username = (String) sessionAttributes.get("username");
            Long chatId = (Long) sessionAttributes.get("chatId");

            // Set user status to OFFLINE
            if (username != null) {
                User user = userRepository.findByUsername(username).orElse(null);
                if (user != null) {
                    user.setStatus(UserStatus.OFFLINE);
                    userRepository.save(user);
                }
            }

            if (username != null && chatId != null) {
                log.info("User disconnected unexpectedly: {} from room: {}", username, chatId);

                // Create leave notification
                ChatMessageDTO leaveMessage = ChatMessageDTO.builder()
                        .type(MessageType.LEAVE)
                        .senderName(username)
                        .chatId(chatId)
                        .content(username + " has disconnected")
                        .build();

                // Notify others in the chat room
                messagingTemplate.convertAndSend(
                    "/topic/chat." + chatId,
                    leaveMessage
                );
            }
        }
    }
}