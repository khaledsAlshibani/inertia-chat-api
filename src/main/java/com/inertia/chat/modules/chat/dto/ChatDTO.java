package com.inertia.chat.modules.chat.dto;

import java.util.List;

import lombok.Data;

@Data
public class ChatDTO {
    private Long id;
    private String type; 
    private List<ChatParticipantDTO> participants;
    private ChatMessageDTO lastMessage;
}