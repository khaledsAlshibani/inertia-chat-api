package com.inertia.chat.modules.chat.dto;

import com.inertia.chat.modules.chat.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private String content;
    private Long senderId;
    private String senderName;
    private String senderProfilePicture;
    private Long chatId;
    private LocalDateTime createdAt;
    private MessageType type;
    private List<AttachmentDTO> attachments;
}