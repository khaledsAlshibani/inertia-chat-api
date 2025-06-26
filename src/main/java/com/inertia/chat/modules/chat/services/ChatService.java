package com.inertia.chat.modules.chat.services;

import com.inertia.chat.modules.chat.dto.ChatDTO;
import com.inertia.chat.modules.chat.dto.ChatMessageDTO;
import com.inertia.chat.modules.chat.dto.CreateGroupChatDTO;
import com.inertia.chat.modules.chat.dto.GroupParticipantDTO;
import com.inertia.chat.modules.chat.entities.Chat;

import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

public interface ChatService {
    ChatMessageDTO saveMessage(ChatMessageDTO messageDTO);
    ChatMessageDTO saveMessage(Long senderId, Long chatId, String content, List<MultipartFile> attachments);

    List<ChatMessageDTO> getChatHistory(Long chatId);
    List<ChatDTO> getUserChats(Long userId);
    
    Optional<Chat> findOneToOneChat(Long userId1, Long userId2);
    Chat createOneToOneChat(Long userId1, Long userId2);
    
    void deleteChatForUser(Long chatId, Long userId);
    void restoreChatForUser(Long chatId, Long userId);

    ChatDTO createGroupChat(Long creatorId, CreateGroupChatDTO dto, MultipartFile avatar);
    void addParticipant(Long chatId, Long actorId, Long userId);
    void removeParticipant(Long chatId, Long actorId, Long userId);
    List<GroupParticipantDTO> getGroupParticipants(Long chatId, Long requesterId);
    ChatDTO renameGroupChat(Long chatId, Long actorId, String newName);
    void leaveGroup(Long chatId, Long userId);
    void deleteGroup(Long chatId, Long actorId);
    ChatDTO getGroupDetails(Long chatId, Long userId);
    ChatDTO updateGroupAvatar(Long chatId, Long actorId, MultipartFile avatar);
    void changeParticipantRole(Long chatId, Long actorId, Long targetUserId, String newRoleStr);
}