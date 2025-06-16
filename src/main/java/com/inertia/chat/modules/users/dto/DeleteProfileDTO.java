package com.inertia.chat.modules.users.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteProfileDTO {
    @NotBlank(message = "Password is required for account deletion")
    private String password;
}