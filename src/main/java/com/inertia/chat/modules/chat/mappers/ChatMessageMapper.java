package com.inertia.chat.modules.chat.mappers;

import com.inertia.chat.modules.chat.dto.AttachmentDTO;
import com.inertia.chat.modules.chat.dto.ChatMessageDTO;
import com.inertia.chat.modules.chat.entities.Attachment;
import com.inertia.chat.modules.chat.entities.Message;
import com.inertia.chat.modules.chat.enums.MessageType;
import java.util.Collections;
import java.util.stream.Collectors;

public class ChatMessageMapper {

    public static ChatMessageDTO toDTO(Message message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getName());
        dto.setSenderProfilePicture(message.getSender().getProfilePicture());
        dto.setChatId(message.getChat().getId());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setType(MessageType.CHAT);

        // map attachments
        if (message.getAttachments() != null) {
            dto.setAttachments(
                message.getAttachments().stream()
                    .map(ChatMessageMapper::mapToDTO)
                    .collect(Collectors.toList())
            );
        } else {
            dto.setAttachments(Collections.emptyList());
        }

        return dto;
    }

    public static Message toEntity(ChatMessageDTO dto) {
        Message message = Message.builder()
            .content(dto.getContent())
            // sender & chat must be set by caller
            .build();

        // initialize empty attachments list
        message.setAttachments(Collections.emptyList());
        return message;
    }

    /** Helper: map Attachment entity → AttachmentDTO */
    private static AttachmentDTO mapToDTO(Attachment att) {
        AttachmentDTO a = new AttachmentDTO();
        a.setId(att.getId());
        a.setType(att.getType());
        a.setUrl(att.getUrl());
        a.setFileName(att.getFileName());
        return a;
    }

    /** Helper: map AttachmentDTO → Attachment entity (caller must set message & save) */
    public static Attachment toEntity(AttachmentDTO dto) {
        return Attachment.builder()
            .type(dto.getType())
            .url(dto.getUrl())
            .fileName(dto.getFileName())
            // message reference must be set by caller
            .build();
    }
}
