package com.inertia.chat.modules.chat.repositories;

import com.inertia.chat.modules.chat.entities.Message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChatIdOrderByCreatedAtAsc(Long chatId);
    @Query("SELECT m FROM Message m WHERE m.id IN (" +
       " SELECT MAX(m2.id) FROM Message m2 WHERE m2.chat.id IN :chatIds GROUP BY m2.chat.id )")
    List<Message> findLastMessagesForChatIds(@Param("chatIds") List<Long> chatIds);
}