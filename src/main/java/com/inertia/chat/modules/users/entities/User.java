package com.inertia.chat.modules.users.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inertia.chat.common.enums.UserStatus;
import com.inertia.chat.modules.auth.entities.RefreshToken;
import com.inertia.chat.modules.chat.entities.Chat;
import com.inertia.chat.modules.chat.entities.ChatUser;
import com.inertia.chat.modules.chat.entities.Message;
import com.inertia.chat.modules.chat.entities.MessageStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(unique = true, nullable = false)
    private String username;

    private String profilePicture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.OFFLINE;

    private LocalDateTime lastSeen;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<RefreshToken> refreshTokens;

    @OneToMany(mappedBy = "sender")
    private List<Message> messages;

    @OneToMany(mappedBy = "user")
    private List<ChatUser> chats;

    @OneToMany(mappedBy = "creator")
    private List<Chat> createdChats;

    @OneToMany(mappedBy = "user")
    private List<MessageStatus> messageStatuses;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    // DTO for user list responses
    public static class UserListDTO {
        private Long id;
        private String username;
        private String name;
        private com.inertia.chat.common.enums.UserStatus status;

        public UserListDTO(Long id, String username, String name, com.inertia.chat.common.enums.UserStatus status) {
            this.id = id;
            this.username = username;
            this.name = name;
            this.status = status;
        }

        // Getters and setters
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getName() { return name; }
        public com.inertia.chat.common.enums.UserStatus getStatus() { return status; }
    }
}