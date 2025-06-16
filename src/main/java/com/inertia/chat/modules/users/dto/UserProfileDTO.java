package com.inertia.chat.modules.users.dto;

import com.inertia.chat.modules.users.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String profilePicture;
    private UserStatus status;
    private LocalDateTime lastSeen;
}