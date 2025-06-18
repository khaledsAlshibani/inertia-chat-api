package com.inertia.chat.modules.chat.services;

import com.inertia.chat.modules.chat.dto.ChatMessageDTO;
import com.inertia.chat.modules.chat.dto.UpdateMessageRequest;

public interface MessageService {
    ChatMessageDTO updateMessage(Long messageId, UpdateMessageRequest request, Long currentUserId);
    void deleteMessage(Long messageId, Long currentUserId);
    void markAsRead(Long messageId, Long currentUserId);
}
