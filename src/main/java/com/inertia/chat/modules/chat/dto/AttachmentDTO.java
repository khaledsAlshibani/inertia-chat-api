package com.inertia.chat.modules.chat.dto;

import com.inertia.chat.modules.chat.enums.AttachmentType;

import lombok.Data;

@Data
public class AttachmentDTO {
    private Long id;
    private AttachmentType type;
    private String url;
    private String fileName;
}
