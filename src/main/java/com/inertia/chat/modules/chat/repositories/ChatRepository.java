package com.inertia.chat.modules.chat.repositories;

import com.inertia.chat.modules.chat.entities.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
}