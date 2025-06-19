package com.inertia.chat.modules.chat.dto;

import java.time.LocalDateTime;

import com.inertia.chat.modules.users.enums.UserRole;

import lombok.Data;

@Data
public class ChatParticipantDTO {
    private Long id;               
    private String username;
    private String name;
    private String profilePicture;
    private UserRole role;         
    private LocalDateTime joinedAt; 
    private LocalDateTime lastSeen; 
}