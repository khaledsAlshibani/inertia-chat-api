package com.inertia.chat.modules.chat.services.impl;

import com.inertia.chat.modules.users.enums.UserRole;
import com.inertia.chat.common.exceptions.ValidationException;
import com.inertia.chat.modules.chat.dto.AttachmentDTO;
import com.inertia.chat.modules.chat.dto.ChatDTO;
import com.inertia.chat.modules.chat.dto.ChatMessageDTO;
import com.inertia.chat.modules.chat.dto.CreateGroupChatDTO;
import com.inertia.chat.modules.chat.dto.GroupParticipantDTO;
import com.inertia.chat.modules.chat.entities.Attachment;
import com.inertia.chat.modules.chat.entities.Chat;
import com.inertia.chat.modules.chat.entities.ChatUser;
import com.inertia.chat.modules.chat.entities.ChatUserId;
import com.inertia.chat.modules.chat.entities.Message;
import com.inertia.chat.modules.chat.entities.MessageStatus;
import com.inertia.chat.modules.chat.enums.ChatType;
import com.inertia.chat.modules.chat.enums.MessageStatusType;
import com.inertia.chat.modules.chat.enums.MessageType;
import com.inertia.chat.modules.chat.events.MessageCreatedEvent;
import com.inertia.chat.modules.chat.mappers.ChatMapper;
import com.inertia.chat.modules.chat.mappers.ChatMessageMapper;
import com.inertia.chat.modules.chat.repositories.AttachmentRepository;
import com.inertia.chat.modules.chat.repositories.ChatRepository;
import com.inertia.chat.modules.chat.repositories.MessageRepository;
import com.inertia.chat.modules.chat.repositories.ChatUserRepository;
import com.inertia.chat.modules.chat.services.AttachmentService;
import com.inertia.chat.modules.chat.services.ChatService;
import com.inertia.chat.modules.chat.storage.FileStorage;
import com.inertia.chat.modules.users.entities.User;
import com.inertia.chat.modules.users.repositories.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

        private final ChatRepository chatRepository;
        private final MessageRepository messageRepository;
        private final UserRepository userRepository;
        private final ChatUserRepository chatUserRepository;
        private final AttachmentRepository attachmentRepository;
        private final AttachmentService attachmentService;
        private final ApplicationEventPublisher eventPublisher;
        private final FileStorage fileStorage;

        @Override
        @Transactional
        public ChatMessageDTO saveMessage(
                Long senderId,
                Long chatId,
                String content,
                List<MultipartFile> attachments) {
                // Validate input
                if ((content == null || content.isBlank())
                                && (attachments == null || attachments.isEmpty())) {
                        throw new IllegalArgumentException("Message content or attachments are required");
                }

                User sender = userRepository.findById(senderId)
                                .orElseThrow(() -> new RuntimeException("User not found: " + senderId));
                Chat chat = chatRepository.findById(chatId)
                                .orElseThrow(() -> new RuntimeException("Chat not found: " + chatId));

                // Upload & map attachments to entities
                List<Attachment> attachmentEntities = attachmentService.processAttachments(attachments);

                Message message = Message.builder()
                        .sender(sender)
                        .chat(chat)
                        .content(content)
                        .attachments(attachmentEntities)
                        .build();

                // Set the message for each attachment entity
                attachmentEntities.forEach(att -> att.setMessage(message));
                message.setAttachments(attachmentEntities);

                // Initialize read‑status for every *other* participant
                List<MessageStatus> statuses = chat.getParticipants().stream()
                        .map(ChatUser::getUser)
                        .filter(u -> !u.getId().equals(senderId))
                        .map(u -> MessageStatus.builder()
                                .message(message)
                                .user(u)
                                .status(MessageStatusType.SENT)
                                .build())
                        .collect(Collectors.toList());
                
                message.setReadStatus(statuses);

                // Save the message (will cascade‐save attachments)
                Message saved = messageRepository.save(message);

                ChatMessageDTO savedDto = ChatMessageMapper.toDTO(saved);
                eventPublisher.publishEvent(new MessageCreatedEvent(savedDto));
                return savedDto;
        }

        @Transactional
        public ChatMessageDTO saveMessage(ChatMessageDTO messageDTO) {
                User sender = userRepository.findById(messageDTO.getSenderId())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Chat chat = chatRepository.findById(messageDTO.getChatId())
                                .orElseGet(() -> {
                                        Chat newChat = Chat.builder()
                                                        .creator(sender)
                                                        .build();
                                        return chatRepository.save(newChat);
                                });

                Message message = ChatMessageMapper.toEntity(messageDTO);
                message.setSender(sender);
                message.setChat(chat);

                Message savedMessage = messageRepository.save(message);

                if (messageDTO.getAttachments() != null) {
                        messageDTO.getAttachments().stream()
                                        .map(ChatMessageMapper::toEntity)
                                        .forEach(a -> {
                                                a.setMessage(savedMessage);
                                                attachmentRepository.save(a);
                                        });
                }

                return ChatMessageMapper.toDTO(savedMessage);
        }

        @Override
        public List<ChatMessageDTO> getChatHistory(Long chatId) {
                // Get the chat and check if it exists
                Chat chat = chatRepository.findById(chatId)
                                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

                // Get the current user from security context
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                User currentUser = userRepository.findByUsername(username)
                                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));

                ChatUser chatUser = chatUserRepository.findByUserAndChat(currentUser, chat)
                                .orElseThrow(() -> new AccessDeniedException("User is not a participant in this chat"));

                List<Message> messages;
                if (chatUser.getDeletedAt() != null) {
                        // If chat has a deletion timestamp (whether currently deleted or restored),
                        // only return messages after deletion time
                        messages = messageRepository.findByChatIdAndCreatedAtAfterOrderByCreatedAtAsc(
                                        chatId,
                                        chatUser.getDeletedAt());
                } else {
                        // If chat has never been deleted, return all messages
                        messages = messageRepository.findByChatIdOrderByCreatedAtAsc(chatId);
                }

                return messages.stream()
                                .map(message -> ChatMessageMapper.toDTO(message))
                                .collect(Collectors.toList());
        }

        @Override
        public List<ChatDTO> getUserChats(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                List<ChatUser> chatUsers = chatUserRepository.findByUserAndIsDeletedFalse(user);
                List<Chat> chats = chatUsers.stream().map(ChatUser::getChat).toList();

                List<Long> chatIds = chats.stream().map(Chat::getId).toList();
                List<Message> lastMessages = messageRepository.findLastMessagesForChatIds(chatIds);

                Map<Long, Message> lastMessageMap = lastMessages.stream()
                                .collect(Collectors.toMap(m -> m.getChat().getId(), m -> m));

                return chats.stream()
                                .map(chat -> {
                                        // Get the chat user record to check deletion timestamp
                                        ChatUser chatUser = chatUserRepository.findByUserAndChat(user, chat)
                                                        .orElseThrow(() -> new RuntimeException(
                                                                        "User is not a participant in this chat"));

                                        Message lastMessage = lastMessageMap.get(chat.getId());

                                        // If there's a deletion timestamp and the last message is before it, don't show
                                        // the message
                                        if (chatUser.getDeletedAt() != null && lastMessage != null &&
                                                        lastMessage.getCreatedAt().isBefore(chatUser.getDeletedAt())) {
                                                lastMessage = null;
                                        }

                                        return ChatMapper.toDTO(chat, lastMessage);
                                })
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public Optional<Chat> findOneToOneChat(Long userId1, Long userId2) {
                List<Chat> chats = chatRepository.findByParticipantsUserId(userId1);
                return chats.stream()
                                .filter(chat -> {
                                        List<ChatUser> participants = chat.getParticipants();
                                        return participants.size() == 2 &&
                                                        ((participants.get(0).getUser().getId().equals(userId1)
                                                                        && participants.get(1).getUser().getId()
                                                                                        .equals(userId2))
                                                                        ||
                                                                        (participants.get(0).getUser().getId()
                                                                                        .equals(userId2)
                                                                                        && participants.get(1).getUser()
                                                                                                        .getId()
                                                                                                        .equals(userId1)));
                                })
                                .findFirst()
                                .map(chat -> {
                                        // Get authentication from security context with null check
                                        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                                        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
                                                throw new RuntimeException("Invalid authentication state");
                                        }

                                        // Get current user and verify it's still valid in database
                                        User currentUser = (User) authentication.getPrincipal();
                                        currentUser = userRepository.findById(currentUser.getId())
                                                .orElseThrow(() -> new RuntimeException("User no longer exists"));

                                        // Find the chat user record for the current user
                                        ChatUser chatUser = chatUserRepository.findByUserAndChat(currentUser, chat)
                                                        .orElseThrow(() -> new RuntimeException(
                                                                        "User is not a participant in this chat"));

                                        // If the chat is deleted for the current user, restore it
                                        if (chatUser.isDeleted() && chatUser.getDeletedAt() != null) {
                                                chatUserRepository.restoreChat(currentUser, chat);
                                                // Refresh the chat user to get updated state
                                                chatUser = chatUserRepository.findByUserAndChat(currentUser, chat)
                                                                .orElseThrow(() -> new RuntimeException(
                                                                                "User is not a participant in this chat"));
                                        }

                                        return chat;
                                });
        }

        @Override
        @Transactional
        public Chat createOneToOneChat(Long userId1, Long userId2) {
                User user1 = userRepository.findById(userId1)
                                .orElseThrow(() -> new RuntimeException("User 1 not found"));
                User user2 = userRepository.findById(userId2)
                                .orElseThrow(() -> new RuntimeException("User 2 not found"));

                Chat chat = Chat.builder()
                                .creator(user1)
                                .type(ChatType.INDIVIDUAL)
                                .build();

                chat = chatRepository.save(chat);

                // Create and save ChatUser for user1
                ChatUserId chatUserId1 = new ChatUserId();
                ChatUser chatUser1 = ChatUser.builder()
                                .id(chatUserId1)
                                .user(user1)
                                .chat(chat)
                                .role(UserRole.MEMBER)
                                .joinedAt(LocalDateTime.now())
                                .build();
                chatUserRepository.save(chatUser1);

                // Create and save ChatUser for user2
                ChatUserId chatUserId2 = new ChatUserId();
                ChatUser chatUser2 = ChatUser.builder()
                                .id(chatUserId2)
                                .user(user2)
                                .chat(chat)
                                .role(UserRole.MEMBER)
                                .joinedAt(LocalDateTime.now())
                                .build();
                chatUserRepository.save(chatUser2);

                return chat;
        }

        @Override
        @Transactional
        public void deleteChatForUser(Long chatId, Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Chat chat = chatRepository.findById(chatId)
                                .orElseThrow(() -> new RuntimeException("Chat not found"));

                chatUserRepository.markAsDeleted(user, chat);
        }

        @Override
        @Transactional
        public void restoreChatForUser(Long chatId, Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Chat chat = chatRepository.findById(chatId)
                                .orElseThrow(() -> new RuntimeException("Chat not found"));

                chatUserRepository.restoreChat(user, chat);
        }
 
        @Override
        @Transactional
        public ChatDTO createGroupChat(
                Long creatorId,
                CreateGroupChatDTO dto,
                MultipartFile avatar
        ) {
                User creator = userRepository.findById(creatorId)
                        .orElseThrow(() -> new EntityNotFoundException("Creator not found"));

                Chat group = Chat.builder()
                        .creator(creator)
                        .type(ChatType.GROUP)
                        .name(dto.getName())
                        .build();

                if (avatar != null && !avatar.isEmpty()) {
                        String url = fileStorage.uploadAvatar(avatar);
                        group.setAvatarUrl(url);
                }

                group = chatRepository.save(group);

                // batch‐fetch users
                Set<Long> allIds = new HashSet<>(dto.getParticipantIds());
                allIds.add(creatorId);
                List<User> users = userRepository.findAllById(allIds);
                if (users.size() != allIds.size()) {
                        Set<Long> found = users.stream()
                                        .map(User::getId)
                                        .collect(Collectors.toSet());
                        allIds.removeAll(found);
                        throw new EntityNotFoundException("Users not found: " + allIds);
                }
                Map<Long, User> userMap = users.stream()
                        .collect(Collectors.toMap(User::getId, Function.identity()));

                // create and save ChatUser links
                LocalDateTime now = LocalDateTime.now();
                List<ChatUser> links = new ArrayList<>();
                for (Long uid : userMap.keySet()) {
                        User u = userMap.get(uid);
                        ChatUser cu = ChatUser.builder()
                        .id(new ChatUserId())
                        .chat(group)
                        .user(u)
                        .role(uid.equals(creatorId) ? UserRole.OWNER : UserRole.MEMBER)
                        .joinedAt(now)
                        .build();
                        links.add(cu);
                }
                chatUserRepository.saveAll(links);

                group.setParticipants(links);

                return ChatMapper.toDTO(group, null);
        }


        @Override
        @Transactional(readOnly = true)
        public ChatDTO getGroupDetails(Long chatId, Long requesterId) {
                Chat chat = validateGroup(chatId, requesterId);
                return ChatMapper.toDTO(chat, null);
        }

        @Override
        @Transactional
        public ChatDTO renameGroupChat(Long chatId, Long actorId, String newName) {
                Chat chat = validateGroup(chatId, actorId);
                ChatUser actorLink = chatUserRepository
                .findByChatIdAndUserId(chatId, actorId)
                .orElseThrow(() -> new AccessDeniedException("Not a participant"));
                if (actorLink.getRole() != UserRole.OWNER) {
                throw new AccessDeniedException("Only OWNER can rename");
                }
                chat.setName(newName);
                chat = chatRepository.save(chat);
                return ChatMapper.toDTO(chat, null);
        }

        @Override
        @Transactional
        public ChatDTO updateGroupAvatar(Long chatId, Long actorId, MultipartFile avatar) {
                Chat chat = validateGroup(chatId, actorId);

                // only OWNER or ADMIN
                ChatUser actorLink = chatUserRepository.findByChatIdAndUserId(chatId, actorId)
                .orElseThrow(() -> new AccessDeniedException("Not a participant"));
                if (actorLink.getRole() == UserRole.MEMBER) {
                throw new AccessDeniedException("Requires ADMIN or OWNER");
                }

                if (avatar == null || avatar.isEmpty()) {
                throw new ValidationException("avatar", "File cannot be empty");
                }

                String url = fileStorage.uploadAvatar(avatar);
                chat.setAvatarUrl(url);
                chat = chatRepository.save(chat);

                return ChatMapper.toDTO(chat, null);
        }

        @Override
        @Transactional(readOnly = true)
        public List<GroupParticipantDTO> getGroupParticipants(Long chatId, Long requesterId) {
                validateGroup(chatId, requesterId);

                Chat chat = chatRepository.findById(chatId)
                        .orElseThrow(() -> new EntityNotFoundException("Chat not found"));
                
                return chat.getParticipants().stream()
                        .map(part -> {
                        var u  = part.getUser();
                        return new GroupParticipantDTO(
                                u.getId(),
                                u.getUsername(),
                                u.getName(),
                                u.getStatus(),
                                u.getLastSeen(),
                                u.getProfilePicture(),
                                part.getRole().name(),    
                                part.getJoinedAt()       
                        );
                        })
                        .toList();
        }


        @Override
        @Transactional
        public void addParticipant(Long chatId, Long actorId, Long userIdToAdd) {
                Chat chat = validateGroup(chatId, actorId);
                ChatUser actorLink = chatUserRepository
                .findByChatIdAndUserId(chatId, actorId)
                .orElseThrow(() -> new AccessDeniedException("Not a participant"));

                if (actorLink.getRole() != UserRole.OWNER
                && actorLink.getRole() != UserRole.ADMIN) {
                throw new AccessDeniedException("Only OWNER or ADMIN can add");
                }

                if (chatUserRepository.existsByChatIdAndUserId(chatId, userIdToAdd)) {
                throw new ValidationException("participant", "Already a participant");
                }

                User u = userRepository.findById(userIdToAdd)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
                ChatUser cu = ChatUser.builder()
                .id(new ChatUserId())
                .chat(chat)
                .user(u)
                .role(UserRole.MEMBER)
                .joinedAt(LocalDateTime.now())
                .build();
                chatUserRepository.save(cu);
        }

        @Override
        @Transactional
        public void removeParticipant(Long chatId, Long actorId, Long userIdToRemove) {
                Chat chat = validateGroup(chatId, actorId);
                ChatUser actorLink = chatUserRepository
                .findByChatIdAndUserId(chatId, actorId)
                .orElseThrow(() -> new AccessDeniedException("Not a participant"));

                if (actorLink.getRole() != UserRole.OWNER
                && actorLink.getRole() != UserRole.ADMIN) {
                throw new AccessDeniedException("Only OWNER or ADMIN can remove");
                }
                if (userIdToRemove.equals(actorId)) {
                throw new ValidationException("participant", "Use leaveGroup to leave yourself");
                }
                chatUserRepository.deleteByChatIdAndUserId(chatId, userIdToRemove);
        }


        @Override
        @Transactional
        public void changeParticipantRole(
                Long chatId,
                Long actorId,
                Long targetUserId,
                String newRoleStr
        ) {
                Chat chat = validateGroup(chatId, actorId);

                // parse & validate
                UserRole newRole;
                try {
                newRole = UserRole.valueOf(newRoleStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                throw new ValidationException("role", "Invalid role: " + newRoleStr);
                }

                ChatUser actorLink = chatUserRepository
                .findByChatIdAndUserId(chatId, actorId)
                .orElseThrow(() -> new AccessDeniedException("Not a participant"));
                if (actorLink.getRole() != UserRole.OWNER) {
                throw new AccessDeniedException("Only OWNER can change roles");
                }

                ChatUser targetLink = chatUserRepository
                .findByChatIdAndUserId(chatId, targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not in chat"));

                // prevent demoting last owner
                if (targetLink.getRole() == UserRole.OWNER && newRole != UserRole.OWNER) {
                long owners = chatUserRepository.countByChatIdAndRole(chatId, UserRole.OWNER);
                if (owners <= 1) {
                        throw new ValidationException("role", "Cannot demote last owner");
                }
                }

                targetLink.setRole(newRole);
                chatUserRepository.save(targetLink);
        }

        @Override
        @Transactional
        public void leaveGroup(Long chatId, Long userId) {
                Chat chat = validateGroup(chatId, userId);
                ChatUser leaving = chatUserRepository
                .findByChatIdAndUserId(chatId, userId)
                .orElseThrow(() -> new EntityNotFoundException("User not in chat"));

                // if owner leaves, transfer or delete
                if (leaving.getRole() == UserRole.OWNER) {
                long remaining = chatUserRepository.countByChatId(chatId);
                if (remaining > 1) {
                        // pick first admin or member to become new owner
                        ChatUser newOwner = chatUserRepository
                        .findFirstByChatIdAndUserIdNotAndRoleIn(
                                chatId, userId, List.of(UserRole.ADMIN, UserRole.MEMBER)
                        )
                        .stream().findFirst()
                        .orElseThrow(() -> new IllegalStateException("No candidate for owner"));
                        newOwner.setRole(UserRole.OWNER);
                        chatUserRepository.save(newOwner);
                } else {
                        // last participant → delete group
                        chatRepository.deleteById(chatId);
                        return;
                }
                }

                // remove the leaving link
                chatUserRepository.delete(leaving);
        }

        @Override
        @Transactional
        public void deleteGroup(Long chatId, Long actorId) {
                ChatUser actor = chatUserRepository
                .findByChatIdAndUserId(chatId, actorId)
                .orElseThrow(() -> new AccessDeniedException("Not a participant"));
                if (actor.getRole() != UserRole.OWNER) {
                throw new AccessDeniedException("Only OWNER can delete group");
                }
                Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));
                chatRepository.deleteById(chatId);
        }

        private Chat validateGroup(Long chatId, Long userId) {
                Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found: " + chatId));
                if (chat.getType() != ChatType.GROUP) {
                throw new IllegalArgumentException("Not a group chat");
                }
                if (!chatUserRepository.existsByChatIdAndUserId(chatId, userId)) {
                throw new AccessDeniedException("Not a participant");
                }
                return chat;
        }

}
