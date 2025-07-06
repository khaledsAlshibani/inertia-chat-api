package com.inertia.chat.modules.chat.controllers;

import com.inertia.chat.modules.chat.dto.ChatDTO;
import com.inertia.chat.modules.chat.dto.ChatMessageDTO;
import com.inertia.chat.modules.chat.dto.CreateGroupChatDTO;
import com.inertia.chat.modules.chat.dto.GroupParticipantDTO;
import com.inertia.chat.modules.chat.entities.Chat;
import com.inertia.chat.modules.chat.services.ChatService;
import com.inertia.chat.modules.users.entities.User;

import jakarta.validation.Valid;

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

    @GetMapping()
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

    @PostMapping(value = "/group", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EnvelopeResponse<ChatDTO>> createGroupChat(
        @AuthenticationPrincipal User currentUser,
        @RequestPart("dto") @Valid CreateGroupChatDTO dto,
        @RequestPart(value = "avatar", required = false) MultipartFile avatar
    ) {
        ChatDTO group = chatService.createGroupChat(currentUser.getId(), dto, avatar);
        return ResponseEntity.ok(EnvelopeResponse.success(group, "Group created"));
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<EnvelopeResponse<ChatDTO>> getGroupDetails(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long chatId
    ) {
        ChatDTO dto = chatService.getGroupDetails(chatId, currentUser.getId());
        return ResponseEntity.ok(EnvelopeResponse.success(dto, "Group details fetched"));
    }

    @PostMapping("/{chatId}/participants/{userId}")
    public ResponseEntity<EnvelopeResponse<Void>> addParticipant(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long chatId,
            @PathVariable Long userId) {
        chatService.addParticipant(chatId, currentUser.getId(), userId);
        return ResponseEntity.ok(EnvelopeResponse.success(null, "Participant added"));
    }

    @DeleteMapping("/{chatId}/participants/{userId}")
    public ResponseEntity<EnvelopeResponse<Void>> removeParticipant(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long chatId,
            @PathVariable Long userId) {
        chatService.removeParticipant(chatId, currentUser.getId(), userId);
        return ResponseEntity.ok(EnvelopeResponse.success(null, "Participant removed"));
    }

    @GetMapping("/{chatId}/participants")
    public ResponseEntity<EnvelopeResponse<List<GroupParticipantDTO>>> getParticipants(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long chatId) {
        List<GroupParticipantDTO> users = chatService.getGroupParticipants(chatId, currentUser.getId());
        return ResponseEntity.ok(EnvelopeResponse.success(users, "Participants fetched"));
    }

    @PatchMapping("/{chatId}/participants/{userId}/role")
    public ResponseEntity<EnvelopeResponse<Void>> changeRole(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long chatId,
            @PathVariable Long userId,
            @RequestParam String newRole) {
        chatService.changeParticipantRole(chatId, currentUser.getId(), userId, newRole);
        return ResponseEntity.ok(EnvelopeResponse.success(null, "User role updated"));
    }

    @PatchMapping("/{chatId}/name")
    public ResponseEntity<EnvelopeResponse<ChatDTO>> renameGroup(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long chatId,
            @RequestParam String name
    ) {
        ChatDTO updated = chatService.renameGroupChat(chatId, currentUser.getId(), name);
        return ResponseEntity.ok(EnvelopeResponse.success(updated, "Group renamed"));
    }

    @PatchMapping(value = "/{chatId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EnvelopeResponse<ChatDTO>> updateGroupAvatar(
        @AuthenticationPrincipal User currentUser,
        @PathVariable Long chatId,
        @RequestPart("avatar") MultipartFile avatar
        ) {
        ChatDTO updated = chatService.updateGroupAvatar(chatId, currentUser.getId(), avatar);
        return ResponseEntity.ok(EnvelopeResponse.success(updated, "Avatar updated"));
    }

    @DeleteMapping("/{chatId}/leave")
    public ResponseEntity<EnvelopeResponse<Void>> leaveGroup(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long chatId
    ) {
        chatService.leaveGroup(chatId, currentUser.getId());
        return ResponseEntity.ok(EnvelopeResponse.success(null, "Left group successfully"));
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<EnvelopeResponse<Void>> deleteChat(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long chatId
    ) {
        try {
            // Try to delete as group first
            chatService.deleteGroup(chatId, currentUser.getId());
            return ResponseEntity.ok(EnvelopeResponse.success(null, "Group deleted"));
        } catch (Exception e) {
            // If not a group or not authorized as group admin, try to delete as one-to-one chat
            chatService.deleteChatForUser(chatId, currentUser.getId());
            return ResponseEntity.ok(EnvelopeResponse.success(null, "Chat deleted for user"));
        }
    }
}