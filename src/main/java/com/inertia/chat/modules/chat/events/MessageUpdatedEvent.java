package com.inertia.chat.modules.chat.events;

import com.inertia.chat.modules.chat.dto.ChatMessageDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MessageUpdatedEvent extends ApplicationEvent {
    private final ChatMessageDTO message;

    public MessageUpdatedEvent(Object source, ChatMessageDTO message) {
        super(source);
        this.message = message;
    }
}
