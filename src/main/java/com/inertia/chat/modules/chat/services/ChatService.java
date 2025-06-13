package com.inertia.chat.modules.chat.services;

import com.inertia.chat.modules.chat.dto.ChatDTO;
import com.inertia.chat.modules.chat.dto.ChatMessageDTO;
import com.inertia.chat.modules.chat.entities.Chat;
import com.inertia.chat.modules.chat.entities.Message;

import java.util.List;
import java.util.Optional;

public interface ChatService {
    ChatMessageDTO saveMessage(ChatMessageDTO messageDTO);
    List<ChatMessageDTO> getChatHistory(Long chatId);
    List<ChatDTO> getUserChats(Long userId);
    
    Optional<Chat> findOneToOneChat(Long userId1, Long userId2);
    Chat createOneToOneChat(Long userId1, Long userId2);
    
    void deleteChatForUser(Long chatId, Long userId);
}