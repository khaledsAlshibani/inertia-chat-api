package com.inertia.chat.modules.chat.controllers;

import com.inertia.chat.modules.chat.dto.ChatDTO;
import com.inertia.chat.modules.chat.dto.ChatMessageDTO;
import com.inertia.chat.modules.chat.entities.Chat;
import com.inertia.chat.modules.chat.services.ChatService;
import com.inertia.chat.modules.users.entities.User;
import com.inertia.chat.common.dto.EnvelopeResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


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

    @PostMapping(value = "/{chatId}/messages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<EnvelopeResponse<ChatMessageDTO>> send(
                @AuthenticationPrincipal User user,
                @PathVariable Long chatId,
                @RequestParam(required = false) String content,
                @RequestPart(required = false) List<MultipartFile> attachments
        ) {
            try {
                ChatMessageDTO savedMessage = chatService.saveMessage(user.getId(), chatId, content, attachments);
                return ResponseEntity.ok(EnvelopeResponse.success(savedMessage, "Message sent successfully"));
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                        .body(EnvelopeResponse.error(List.of("Failed to send message: " + e.getMessage())));
            }
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