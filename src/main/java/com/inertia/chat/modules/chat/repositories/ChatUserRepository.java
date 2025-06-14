package com.inertia.chat.modules.chat.repositories;

import com.inertia.chat.modules.chat.entities.ChatUser;
import com.inertia.chat.modules.chat.entities.ChatUserId;
import com.inertia.chat.modules.chat.entities.Chat;
import com.inertia.chat.modules.users.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatUserRepository extends JpaRepository<ChatUser, ChatUserId> {
    List<ChatUser> findByUserAndIsDeletedFalse(User user);
    
    Optional<ChatUser> findByUserAndChat(User user, Chat chat);
    
    @Modifying
    @Query("UPDATE ChatUser cu SET cu.isDeleted = true, cu.deletedAt = CURRENT_TIMESTAMP WHERE cu.user = ?1 AND cu.chat = ?2")
    void markAsDeleted(User user, Chat chat);

    @Modifying
    @Query("UPDATE ChatUser cu SET cu.isDeleted = false WHERE cu.user = ?1 AND cu.chat = ?2 AND cu.deletedAt IS NOT NULL")
    void restoreChat(User user, Chat chat);
}