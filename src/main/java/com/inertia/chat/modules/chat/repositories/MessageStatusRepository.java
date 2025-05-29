package com.inertia.chat.modules.chat.repositories;

import com.inertia.chat.modules.chat.entities.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageStatusRepository extends JpaRepository<MessageStatus, Long> {
    Optional<MessageStatus> findByMessageIdAndUserId(Long messageId, Long userId);
}