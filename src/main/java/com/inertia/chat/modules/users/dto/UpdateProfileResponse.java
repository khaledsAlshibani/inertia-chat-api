package com.inertia.chat.modules.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateProfileResponse {
    private UserListDTO user;
    private String newAccessToken;
} 