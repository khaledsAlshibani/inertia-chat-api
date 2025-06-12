package com.inertia.chat.modules.chat.mappers;

import com.inertia.chat.modules.chat.dto.ChatMessageDTO;
import com.inertia.chat.modules.chat.entities.Message;
import com.inertia.chat.modules.chat.enums.MessageType;

public class ChatMessageMapper {
    public static ChatMessageDTO toDTO(Message message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setSenderId(message.getSender().getId());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setChatId(message.getChat().getId());
        dto.setSenderName(message.getSender().getName());
        dto.setSenderProfilePicture(message.getSender().getProfilePicture());
        dto.setType(MessageType.CHAT);
        return dto;
    }
}
