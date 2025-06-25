package com.inertia.chat.modules.chat.entities;

import com.inertia.chat.modules.chat.enums.ChatType;
import com.inertia.chat.modules.users.entities.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chats")
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    private String name;

    @OneToMany(mappedBy = "chat")
    private List<ChatUser> participants;

    @OneToMany(mappedBy = "chat")
    private List<Message> messages;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatType type = ChatType.INDIVIDUAL;
}