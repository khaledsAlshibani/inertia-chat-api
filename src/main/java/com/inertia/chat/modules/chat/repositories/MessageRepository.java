package com.inertia.chat.modules.chat.repositories;

import com.inertia.chat.modules.chat.entities.Message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND m.deleted = false ORDER BY m.createdAt ASC")
    List<Message> findByChatIdOrderByCreatedAtAsc(@Param("chatId") Long chatId);

    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND m.createdAt >= :afterDate AND m.deleted = false ORDER BY m.createdAt ASC")
    List<Message> findByChatIdAndCreatedAtAfterOrderByCreatedAtAsc(
        @Param("chatId") Long chatId,
        @Param("afterDate") LocalDateTime afterDate
    );

    @Query("SELECT m FROM Message m WHERE m.id IN (" +
       " SELECT MAX(m2.id) FROM Message m2 WHERE m2.chat.id IN :chatIds AND m2.deleted = false GROUP BY m2.chat.id )")
    List<Message> findLastMessagesForChatIds(@Param("chatIds") List<Long> chatIds);

}