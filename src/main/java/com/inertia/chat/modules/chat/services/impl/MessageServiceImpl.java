package com.inertia.chat.modules.chat.services.impl;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inertia.chat.modules.chat.dto.ChatMessageDTO;
import com.inertia.chat.modules.chat.dto.MessageStatusDTO;
import com.inertia.chat.modules.chat.dto.UpdateMessageRequest;
import com.inertia.chat.modules.chat.entities.Message;
import com.inertia.chat.modules.chat.entities.MessageStatus;
import com.inertia.chat.modules.chat.enums.MessageStatusType;
import com.inertia.chat.modules.chat.events.MessageCreatedEvent;
import com.inertia.chat.modules.chat.events.MessageDeletedEvent;
import com.inertia.chat.modules.chat.events.MessageStatusUpdatedEvent;
import com.inertia.chat.modules.chat.events.MessageUpdatedEvent;
import com.inertia.chat.modules.chat.mappers.ChatMessageMapper;
import com.inertia.chat.modules.chat.repositories.ChatRepository;
import com.inertia.chat.modules.chat.repositories.MessageRepository;
import com.inertia.chat.modules.chat.repositories.MessageStatusRepository;
import com.inertia.chat.modules.chat.services.MessageService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final MessageStatusRepository messageStatusRepository;
    private final ApplicationEventPublisher eventPublisher; 

    @Override
    @Transactional
    public ChatMessageDTO updateMessage(Long messageId,
                                        UpdateMessageRequest request,
                                        Long userId) {
        Message msg = messageRepository.findById(messageId)
            .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        if (!msg.getSender().getId().equals(userId)) {
            throw new AccessDeniedException("Not authorized to edit this message");
        }

        msg.setContent(request.getContent());
        msg.setEditedAt(LocalDateTime.now());
        Message saved = messageRepository.save(msg);  

        ChatMessageDTO dto = ChatMessageMapper.toDTO(saved);
        eventPublisher.publishEvent(new MessageUpdatedEvent(this, dto));
        return dto;
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        Message msg = messageRepository.findById(messageId)
            .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        if (!msg.getSender().getId().equals(userId)) {
            throw new AccessDeniedException("Not authorized to delete this message");
        }

        Long chatId = msg.getChat().getId();
        
        // this triggers the @SQLDelete on the entity, setting deleted = true & deletedAt = now()
        messageRepository.delete(msg);

        eventPublisher.publishEvent(new MessageDeletedEvent(this, chatId, messageId));
    }

    @Transactional
    public void markAsRead(Long messageId, Long currentUserId) {
        MessageStatus status = messageStatusRepository
            .findByMessageIdAndUserId(messageId, currentUserId)
            .orElseThrow(() -> new EntityNotFoundException("Status not found"));

        if (status.getStatus() == MessageStatusType.READ) {
            return;
        }

        status.setStatus(MessageStatusType.READ);
        messageStatusRepository.save(status);

        Long chatId = status.getMessage().getChat().getId();

        MessageStatusDTO statusDto = MessageStatusDTO.builder()
            .userId(currentUserId)
            .status(status.getStatus())
            .readAt(status.getReadAt())
            .deliveredAt(status.getDeliveredAt())
            .build();

        eventPublisher.publishEvent(
            new MessageStatusUpdatedEvent(this, chatId, statusDto)
        );
    }
}

