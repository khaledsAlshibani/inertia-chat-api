package com.inertia.chat.modules.chat.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MessageDeletedEvent extends ApplicationEvent {
    private final Long chatId;
    private final Long messageId;

    public MessageDeletedEvent(Object source, Long chatId, Long messageId) {
        super(source);
        this.chatId   = chatId;
        this.messageId = messageId;
    }
}
