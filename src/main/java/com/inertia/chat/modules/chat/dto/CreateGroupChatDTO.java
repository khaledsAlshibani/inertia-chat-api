package com.inertia.chat.modules.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateGroupChatDTO {
    
    @NotBlank(message = "Group name must not be blank")
    @Size(max = 100, message = "Group name must be at most 100 characters")
    private String name;

    @NotEmpty(message = "You must invite at least one participant")
    private List<
        @NotNull(message = "Participant ID cannot be null")
        @Positive(message = "Participant ID must be a positive number")
        Long
    > participantIds;
}
