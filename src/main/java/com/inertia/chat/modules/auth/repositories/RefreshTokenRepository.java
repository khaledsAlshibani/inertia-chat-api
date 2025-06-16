package com.inertia.chat.modules.auth.repositories;

import com.inertia.chat.modules.auth.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByUserId(Long userId);
    List<RefreshToken> findByUserIdOrderByCreatedAtDesc(Long userId);
    void deleteByUserId(Long userId);
}