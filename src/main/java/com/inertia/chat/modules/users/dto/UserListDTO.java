package com.inertia.chat.modules.users.dto;

import com.inertia.chat.modules.users.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListDTO {
    private Long id;
    private String username;
    private String name;
    private UserStatus status;
}