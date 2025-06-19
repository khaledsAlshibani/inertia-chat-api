package com.inertia.chat.modules.chat.events;

import org.springframework.context.ApplicationEvent;
import com.inertia.chat.modules.chat.dto.MessageStatusDTO;

public class MessageStatusUpdatedEvent extends ApplicationEvent {
    private final Long chatId;
    private final MessageStatusDTO updatedStatus;

    public MessageStatusUpdatedEvent(Object source,
                                     Long chatId,
                                     MessageStatusDTO updatedStatus) {
        super(source);
        this.chatId = chatId;
        this.updatedStatus = updatedStatus;
    }

    public Long getChatId() {
        return chatId;
    }

    public MessageStatusDTO getUpdatedStatus() {
        return updatedStatus;
    }
}
