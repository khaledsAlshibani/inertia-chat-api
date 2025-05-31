package com.inertia.chat.modules.chat.controllers;

import com.inertia.chat.modules.chat.dto.ChatMessageDTO;
import com.inertia.chat.modules.chat.enums.MessageType;
import com.inertia.chat.modules.chat.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDTO chatMessage) {
        System.out.println("The message has been received");

        // Save message to database
        ChatMessageDTO savedMessage = chatService.saveMessage(chatMessage);

        System.out.println("The message has been received and saved");
        
        // Send to specific chat room
        messagingTemplate.convertAndSend(
            "/topic/chat." + chatMessage.getChatId(),
            savedMessage
        );
    }

    @MessageMapping("/chat.join")
    public void joinChat(@Payload ChatMessageDTO chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // Add user to WebSocket session - store chatId as Long
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSenderName());
        headerAccessor.getSessionAttributes().put("chatId", chatMessage.getChatId());
        
        // Notify others in the chat room
        messagingTemplate.convertAndSend(
            "/topic/chat." + chatMessage.getChatId(),
            chatMessage
        );
    }

    @MessageMapping("/chat.leave")
    public void leaveChat(@Payload ChatMessageDTO chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // Get user info from session
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

        sessionAttributes.put("gracefulExit", true);
        
        if (sessionAttributes != null) {
            String username = (String) sessionAttributes.get("username");
            Long chatId = (Long) sessionAttributes.get("chatId");  // Retrieve as Long
            
            if (username != null && chatId != null) {
                System.out.println("User leaving: " + username + " from room: " + chatId);
                
                // Create leave notification
                ChatMessageDTO leaveMessage = new ChatMessageDTO();
                leaveMessage.setType(MessageType.LEAVE);
                leaveMessage.setSenderName(username);
                leaveMessage.setChatId(chatId);
                leaveMessage.setContent(username + " has left the chat");
                
                // Notify others in the chat room
                messagingTemplate.convertAndSend(
                    "/topic/chat." + chatId,
                    leaveMessage
                );
                
                // Clean up session attributes
                sessionAttributes.remove("username");
                sessionAttributes.remove("chatId");
            }
        }
    }
}