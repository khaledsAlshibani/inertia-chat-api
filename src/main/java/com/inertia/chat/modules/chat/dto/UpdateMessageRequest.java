package com.inertia.chat.modules.chat.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class UpdateMessageRequest {
    @NotBlank(message = "Content must not be empty")
    private String content;
}
