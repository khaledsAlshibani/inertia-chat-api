package com.inertia.chat.modules.chat.mappers;

import com.inertia.chat.modules.chat.dto.ChatDTO;
import com.inertia.chat.modules.chat.dto.ChatParticipantDTO;
import com.inertia.chat.modules.chat.entities.Chat;
import com.inertia.chat.modules.chat.entities.ChatUser;
import com.inertia.chat.modules.chat.entities.Message;
import com.inertia.chat.modules.users.entities.User;

import java.util.List;
import java.util.stream.Collectors;

public class ChatMapper {

    public static ChatDTO toDTO(Chat chat, Message lastMessage) {
        ChatDTO dto = new ChatDTO();
        dto.setId(chat.getId());
        dto.setType(chat.getType().name());

        // Map participants using ChatParticipantDTO
        List<ChatParticipantDTO> participants = chat.getParticipants()
                .stream()
                .map(ChatMapper::mapChatParticipant)
                .collect(Collectors.toList());
        dto.setParticipants(participants);

        // Map last message if present
        if (lastMessage != null) {
            dto.setLastMessage(ChatMessageMapper.toDTO(lastMessage));
        } else {
            dto.setLastMessage(null);
        }

        return dto;
    }

    private static ChatParticipantDTO mapChatParticipant(ChatUser chatUser) {
        User user = chatUser.getUser();

        ChatParticipantDTO dto = new ChatParticipantDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setRole(chatUser.getRole());
        dto.setJoinedAt(chatUser.getJoinedAt());

        return dto;
    }
}
