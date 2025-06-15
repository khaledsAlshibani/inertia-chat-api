package com.inertia.chat.modules.chat.events;

import com.inertia.chat.modules.chat.dto.ChatMessageDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageCreatedEvent {
    private final ChatMessageDTO messageDTO;
}
