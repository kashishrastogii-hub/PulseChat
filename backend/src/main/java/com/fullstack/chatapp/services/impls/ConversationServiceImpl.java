package com.fullstack.chatapp.services.impls;

import com.fullstack.chatapp.dto.responses.ConversationResponseDto;
import com.fullstack.chatapp.exceptions.ResourceNotFoundException;
import com.fullstack.chatapp.exceptions.UnauthorizedConversationException;
import com.fullstack.chatapp.models.Conversation;
import com.fullstack.chatapp.models.ConversationParticipant;
import com.fullstack.chatapp.models.User;
import com.fullstack.chatapp.repositories.ConversationRepository;
import com.fullstack.chatapp.repositories.MessageRepository;
import com.fullstack.chatapp.repositories.UserRepository;
import com.fullstack.chatapp.services.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Override
    @Transactional
    public ConversationResponseDto createOrGetDirectConversation(String currentUsername, String otherUsername) {
        User me = requireUser(currentUsername);
        if (me.getUsername().equalsIgnoreCase(otherUsername)) {
            throw new IllegalArgumentException("You cannot start a conversation with yourself");
        }
        User other = userRepository.findByUsername(otherUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + otherUsername));

        Conversation conversation = conversationRepository.findDirectConversation(me.getId(), other.getId())
                .orElseGet(() -> createDirectConversation(me, other));
        return toDto(conversation, me);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponseDto> getConversations(String username) {
        User me = requireUser(username);
        return conversationRepository.findConversationsForUser(me.getId())
                .stream()
                .map(conversation -> toDto(conversation, me))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResponseDto getConversation(Long conversationId, String username) {
        User me = requireUser(username);
        return toDto(requireParticipantConversation(conversationId, me), me);
    }

    @Override
    @Transactional
    public void markAsRead(Long conversationId, String username) {
        User me = requireUser(username);
        requireParticipantConversation(conversationId, me);
        messageRepository.markConversationAsRead(conversationId, me.getId(), LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public String getOtherParticipantUsername(Long conversationId, String username) {
        User me = requireUser(username);
        Conversation conversation = requireParticipantConversation(conversationId, me);
        return otherParticipant(conversation, me).getUsername();
    }

    private Conversation createDirectConversation(User me, User other) {
        LocalDateTime now = LocalDateTime.now();
        Conversation conversation = Conversation.builder()
                .createdAt(now)
                .updatedAt(now)
                .lastMessageAt(now)
                .participants(new HashSet<>())
                .build();
        conversation.getParticipants().add(ConversationParticipant.builder()
                .conversation(conversation).user(me).joinedAt(now).build());
        conversation.getParticipants().add(ConversationParticipant.builder()
                .conversation(conversation).user(other).joinedAt(now).build());
        return conversationRepository.save(conversation);
    }

    private Conversation requireParticipantConversation(Long conversationId, User me) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));
        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(participant -> participant.getUser().getId().equals(me.getId()));
        if (!isParticipant) {
            throw new UnauthorizedConversationException("You are not a participant in this conversation");
        }
        return conversation;
    }

    private User otherParticipant(Conversation conversation, User me) {
        return conversation.getParticipants().stream()
                .map(ConversationParticipant::getUser)
                .filter(user -> !user.getId().equals(me.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Conversation has no other participant"));
    }

    private User requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private ConversationResponseDto toDto(Conversation conversation, User me) {
        User other = otherParticipant(conversation, me);
        return ConversationResponseDto.builder()
                .id(conversation.getId())
                .otherUserId(other.getId())
                .otherUsername(other.getUsername())
                .lastMessagePreview(conversation.getLastMessagePreview())
                .lastMessageAt(conversation.getLastMessageAt())
                .unreadCount(messageRepository.countUnread(conversation.getId(), me.getId()))
                .createdAt(conversation.getCreatedAt())
                .build();
    }
}
