package com.inertia.chat.modules.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.inertia.chat.modules.chat.enums.MessageStatusType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageStatusDTO {
    private Long messageId;
    private Long userId;
    private MessageStatusType status;
    private LocalDateTime deliveredAt;  
    private LocalDateTime readAt;
}