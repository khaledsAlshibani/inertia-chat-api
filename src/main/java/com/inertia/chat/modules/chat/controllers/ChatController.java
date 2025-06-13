package com.inertia.chat.modules.chat.controllers;

import com.inertia.chat.modules.chat.dto.ChatDTO;
import com.inertia.chat.modules.chat.dto.ChatMessageDTO;
import com.inertia.chat.modules.chat.entities.Chat;
import com.inertia.chat.modules.chat.services.ChatService;
import com.inertia.chat.modules.users.entities.User;
import com.inertia.chat.common.dto.EnvelopeResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @GetMapping("{id}/messages")
    public ResponseEntity<EnvelopeResponse<List<ChatMessageDTO>>> getChatMessages(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        List<ChatMessageDTO> messages = chatService.getChatHistory(id);
        return ResponseEntity.ok(EnvelopeResponse.success(messages, "Messages found"));
    }
    public String getMethodName(@RequestParam String param) {
        return new String();
    }

    @GetMapping("/all")
    public ResponseEntity<EnvelopeResponse<List<ChatDTO>>> getAllChats(
            @AuthenticationPrincipal User currentUser) {
        List<ChatDTO> chats = chatService.getUserChats(currentUser.getId());
        
        return ResponseEntity.ok(EnvelopeResponse.success(chats, "Chats found"));
    }
    

    @GetMapping("/with/{userId}")
    public ResponseEntity<EnvelopeResponse<Long>> findOneToOneChat(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long userId) {
        return chatService.findOneToOneChat(currentUser.getId(), userId)
                .map(chat -> ResponseEntity.ok(EnvelopeResponse.success(chat.getId(), "Chat found")))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/with/{userId}")
    public ResponseEntity<EnvelopeResponse<Long>> createOneToOneChat(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long userId) {
        Chat chat = chatService.createOneToOneChat(currentUser.getId(), userId);
        return ResponseEntity.ok(EnvelopeResponse.success(chat.getId(), "Chat created"));
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<EnvelopeResponse<Void>> deleteChatForUser(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long chatId) {
        chatService.deleteChatForUser(chatId, currentUser.getId());
        return ResponseEntity.ok(EnvelopeResponse.success(null, "Chat deleted for user"));
    }
}