package com.inertia.chat.modules.chat.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class ChatUserId implements Serializable {
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "chat_id")
    private Long chatId;
}