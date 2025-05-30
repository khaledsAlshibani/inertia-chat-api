package com.inertia.chat.modules.chat.services.impl;

import com.inertia.chat.modules.chat.dto.ChatMessageDTO;
import com.inertia.chat.modules.chat.entities.Chat;
import com.inertia.chat.modules.chat.entities.ChatUser;
import com.inertia.chat.modules.chat.entities.Message;
import com.inertia.chat.modules.chat.repositories.ChatRepository;
import com.inertia.chat.modules.chat.repositories.MessageRepository;
import com.inertia.chat.modules.chat.services.ChatService;
import com.inertia.chat.modules.users.entities.User;
import com.inertia.chat.modules.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ChatMessageDTO saveMessage(ChatMessageDTO messageDTO) {
        User sender = userRepository.findById(messageDTO.getSenderId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Chat chat = chatRepository.findById(messageDTO.getChatId())
                .orElseThrow(() -> new RuntimeException("Chat not found"));

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
                .type(ChatMessageDTO.MessageType.CHAT)
                .build();
    }

    @Override
    public List<Message> getChatHistory(Long chatId) {
        return messageRepository.findByChatIdOrderByCreatedAtAsc(chatId);
    }

    @Override
    @Transactional
    public Chat createChat(Long userId1, Long userId2) {
        User user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new RuntimeException("User 1 not found"));
        User user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new RuntimeException("User 2 not found"));

        Chat chat = new Chat();
        chat.setCreator(user1);
        chat.setParticipants(new ArrayList<>());
        chat.setMessages(new ArrayList<>());

        // Create ChatUser for user1
        ChatUser chatUser1 = ChatUser.builder()
                .user(user1)
                .chat(chat)
                .build();
        chat.getParticipants().add(chatUser1);

        // Create ChatUser for user2
        ChatUser chatUser2 = ChatUser.builder()
                .user(user2)
                .chat(chat)
                .build();
        chat.getParticipants().add(chatUser2);

        return chatRepository.save(chat);
    }

    @Override
    public List<Chat> getUserChats(Long userId) {
        return chatRepository.findByParticipantsUserId(userId);
    }
}