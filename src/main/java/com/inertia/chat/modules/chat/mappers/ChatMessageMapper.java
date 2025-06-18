package com.inertia.chat.modules.chat.mappers;

import com.inertia.chat.modules.chat.dto.AttachmentDTO;
import com.inertia.chat.modules.chat.dto.ChatMessageDTO;
import com.inertia.chat.modules.chat.dto.MessageStatusDTO;
import com.inertia.chat.modules.chat.entities.Attachment;
import com.inertia.chat.modules.chat.entities.Message;
import com.inertia.chat.modules.chat.enums.AttachmentType;
import com.inertia.chat.modules.chat.enums.MessageType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

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
            dto.setEditedAt(message.getEditedAt());
            dto.setType(MessageType.CHAT);

            // map attachments
            if (message.getAttachments() != null) {
                dto.setAttachments(
                    message.getAttachments().stream()
                        .map(ChatMessageMapper::toAttachmentDTO)
                        .collect(Collectors.toList())
                );
            } else {
                dto.setAttachments(Collections.emptyList());
            }

            // map per-recipient read status
            if (message.getReadStatus() != null) {
                dto.setStatuses(
                    message.getReadStatus().stream()
                        .map(status -> MessageStatusDTO.builder()
                            .userId(status.getUser().getId())
                            .read(status.isRead())
                            .readAt(status.getReadAt())
                            .build()
                        )
                        .collect(Collectors.toList())
                );
            } else {
                dto.setStatuses(Collections.emptyList());
            }

            return dto;
        }

        public static Message toEntity(ChatMessageDTO dto) {
            Message message = Message.builder()
                .content(dto.getContent())
                // sender & chat must be set by caller
                .build();

            // initialize empty attachments list & statuses
            message.setAttachments(new ArrayList<>());
            message.setReadStatus(new ArrayList<>());
            return message;
        }

        private static AttachmentDTO toAttachmentDTO(Attachment att) {
            AttachmentDTO a = new AttachmentDTO();
            a.setId(att.getId());
            a.setType(att.getType());
            a.setUrl(att.getUrl());
            a.setFileName(att.getFileName());
            return a;
        }

        public static Attachment toAttachmentEntity(MultipartFile file, String url) {
        return Attachment.builder()
            .fileName(file.getOriginalFilename())
            .type(AttachmentType.valueOf(file.getContentType()))
            .url(url)
            .size(file.getSize())
            .build();
        }
        
        public static Attachment toEntity(AttachmentDTO dto) {
            return Attachment.builder()
                .type(dto.getType())
                .url(dto.getUrl())
                .fileName(dto.getFileName())
                // message reference must be set by caller
                .build();
        }
}
