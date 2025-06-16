package com.inertia.chat.modules.users.dto;

import com.inertia.chat.modules.users.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusDTO {
    @NotNull(message = "Status is required")
    private UserStatus status;
}