package com.inertia.chat.modules.chat.dto;

import com.inertia.chat.modules.users.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupParticipantDTO {
    private Long id;
    private String username;
    private String name;
    private UserStatus status;
    private LocalDateTime lastSeen;
    private String profilePicture;
    private String role;
    private LocalDateTime joinedAt;
}