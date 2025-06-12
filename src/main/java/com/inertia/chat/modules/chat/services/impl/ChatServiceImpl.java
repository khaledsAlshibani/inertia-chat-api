package com.inertia.chat.modules.chat.services.impl;

import com.inertia.chat.modules.users.enums.UserRole;
import com.inertia.chat.modules.chat.dto.ChatDTO;
import com.inertia.chat.modules.chat.dto.ChatMessageDTO;
import com.inertia.chat.modules.chat.entities.Chat;
import com.inertia.chat.modules.chat.entities.ChatUser;
import com.inertia.chat.modules.chat.entities.ChatUserId;
import com.inertia.chat.modules.chat.entities.Message;
import com.inertia.chat.modules.chat.enums.ChatType;
import com.inertia.chat.modules.chat.enums.MessageType;
import com.inertia.chat.modules.chat.mappers.ChatMapper;
import com.inertia.chat.modules.chat.mappers.ChatMessageMapper;
import com.inertia.chat.modules.chat.repositories.ChatRepository;
import com.inertia.chat.modules.chat.repositories.MessageRepository;
import com.inertia.chat.modules.chat.repositories.ChatUserRepository;
import com.inertia.chat.modules.chat.services.ChatService;
import com.inertia.chat.modules.users.entities.User;
import com.inertia.chat.modules.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatUserRepository chatUserRepository;

    @Override
    @Transactional
    public ChatMessageDTO saveMessage(ChatMessageDTO messageDTO) {
        User sender = userRepository.findById(messageDTO.getSenderId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Chat chat = chatRepository.findById(messageDTO.getChatId())
                .orElseGet(() -> {
                    // Auto-create chat for 1:1 chat if not found
                    Chat newChat = Chat.builder()
                        .creator(sender)
                        .build();
                    return chatRepository.save(newChat);
                });

        Message message = Message.builder()
                .content(messageDTO.getContent())
                .sender(sender)
                .chat(chat)
                .build();

        Message savedMessage = messageRepository.save(message);
        
        return ChatMessageDTO.builder()
                .id(savedMessage.getId())
                .content(savedMessage.getContent())
                .senderId(savedMessage.getSender().getId())
                .senderName(savedMessage.getSender().getUsername())
                .chatId(savedMessage.getChat().getId())
                .createdAt(savedMessage.getCreatedAt())
                .type(MessageType.CHAT)
                .build();
    }

    @Override
    public List<ChatMessageDTO> getChatHistory(Long chatId) {
    List<Message> messages = messageRepository.findByChatIdOrderByCreatedAtAsc(chatId);

        return messages.stream().map(message -> ChatMessageMapper.toDTO(message))
                .collect(Collectors.toList());
    }


    @Override
    public List<ChatDTO> getUserChats(Long userId) {
        List<Chat> chats = chatRepository.findByParticipantsUserId(userId);

        List<Long> chatIds = chats.stream().map(Chat::getId).toList();
        List<Message> lastMessages = messageRepository.findLastMessagesForChatIds(chatIds);

        Map<Long, Message> lastMessageMap = lastMessages.stream()
                .collect(Collectors.toMap(m -> m.getChat().getId(), m -> m));

        return chats.stream()
                .map(chat -> ChatMapper.toDTO(chat, lastMessageMap.get(chat.getId())))
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
                            ((participants.get(0).getUser().getId().equals(userId1) && participants.get(1).getUser().getId().equals(userId2)) ||
                             (participants.get(0).getUser().getId().equals(userId2) && participants.get(1).getUser().getId().equals(userId1)));
                })
                .findFirst();
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
}
