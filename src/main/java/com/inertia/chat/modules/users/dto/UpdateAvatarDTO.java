package com.inertia.chat.modules.users.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAvatarDTO {
    @NotNull(message = "Avatar file is required")
    private MultipartFile avatar;
}