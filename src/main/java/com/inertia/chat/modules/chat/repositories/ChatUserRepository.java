package com.inertia.chat.modules.chat.repositories;

import com.inertia.chat.modules.chat.entities.ChatUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatUserRepository extends JpaRepository<ChatUser, Long> {
} 