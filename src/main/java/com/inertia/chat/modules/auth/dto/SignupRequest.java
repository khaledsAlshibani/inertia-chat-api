package com.inertia.chat.modules.auth.dto;

import lombok.Data;

@Data
public class SignupRequest {
    private String email;
    private String username;
    private String name;
    private String password;
}
