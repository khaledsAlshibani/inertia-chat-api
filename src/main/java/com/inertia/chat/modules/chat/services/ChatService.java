package com.inertia.chat.modules.chat.services;

import com.inertia.chat.modules.chat.dto.ChatMessageDTO;
import com.inertia.chat.modules.chat.entities.Chat;
import com.inertia.chat.modules.chat.entities.Message;

import java.util.List;

public interface ChatService {
    ChatMessageDTO saveMessage(ChatMessageDTO messageDTO);
    List<Message> getChatHistory(Long chatId);
    Chat createChat(Long userId1, Long userId2);
    List<Chat> getUserChats(Long userId);
}