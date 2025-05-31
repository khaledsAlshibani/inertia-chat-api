package com.inertia.chat.modules.chat.controllers;

import com.inertia.chat.modules.chat.entities.Chat;
import com.inertia.chat.modules.chat.services.ChatService;
import com.inertia.chat.modules.users.entities.User;
import com.inertia.chat.common.dto.EnvelopeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

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
}