package com.inertia.chat.modules.chat.controllers;

import com.inertia.chat.modules.chat.dto.ChatMessageDTO;
import com.inertia.chat.modules.chat.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDTO chatMessage) {
        // Save message to database
        ChatMessageDTO savedMessage = chatService.saveMessage(chatMessage);
        
        // Send to specific chat room
        messagingTemplate.convertAndSend(
            "/topic/chat." + chatMessage.getChatId(),
            savedMessage
        );
    }

    @MessageMapping("/chat.join")
    public void joinChat(@Payload ChatMessageDTO chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // Add user to WebSocket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSenderName());
        headerAccessor.getSessionAttributes().put("chatId", chatMessage.getChatId());

        // Notify others in the chat room
        messagingTemplate.convertAndSend(
            "/topic/chat." + chatMessage.getChatId(),
            chatMessage
        );
    }
}