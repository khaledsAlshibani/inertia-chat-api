package com.inertia.chat.modules.chat.entities;

import com.inertia.chat.modules.chat.enums.MessageStatusType;
import com.inertia.chat.modules.users.entities.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "message_statuses")
public class MessageStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    MessageStatusType status;

    private LocalDateTime deliveredAt;

    private LocalDateTime readAt;

    @PrePersist
    @PreUpdate
    public void updateReadAt() {
        if (status == MessageStatusType.DELIVERED && deliveredAt == null) {
            deliveredAt = LocalDateTime.now();
        }

        if (status == MessageStatusType.READ && readAt == null) {
            readAt = LocalDateTime.now();
        }
    }
}