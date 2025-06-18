package com.inertia.chat.modules.chat.controllers;

import com.inertia.chat.common.dto.EnvelopeResponse;
import com.inertia.chat.modules.chat.dto.ChatMessageDTO;
import com.inertia.chat.modules.chat.dto.UpdateMessageRequest;
import com.inertia.chat.modules.chat.services.MessageService;
import com.inertia.chat.modules.users.entities.User;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PutMapping("/{messageId}")
    public ResponseEntity<EnvelopeResponse<ChatMessageDTO>> updateMessage(
            @PathVariable Long messageId,
            @Valid @RequestBody UpdateMessageRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        ChatMessageDTO updated = messageService.updateMessage(messageId, request, currentUser.getId());
        return ResponseEntity.ok(EnvelopeResponse.success(updated, "Message updated"));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<EnvelopeResponse<Void>> deleteMessage(
            @PathVariable Long messageId,
            @AuthenticationPrincipal User currentUser
    ) {
        messageService.deleteMessage(messageId, currentUser.getId());
        return ResponseEntity.ok(EnvelopeResponse.success(null, "Message deleted"));
    }

    @PostMapping("/{messageId}/read")
    public ResponseEntity<EnvelopeResponse<Void>> markAsRead(@PathVariable Long messageId, @AuthenticationPrincipal User currentUser) {
        messageService.markAsRead(messageId, currentUser.getId());
        return ResponseEntity.ok(EnvelopeResponse.success(null, "Message marked as read"));
    }
    
}
