package com.fullstack.chatapp.services.impls;

import com.fullstack.chatapp.dto.requests.SendMessageRequestDto;
import com.fullstack.chatapp.dto.responses.MessageResponseDto;
import com.fullstack.chatapp.dto.responses.PageResponseDto;
import com.fullstack.chatapp.exceptions.ResourceNotFoundException;
import com.fullstack.chatapp.exceptions.UnauthorizedConversationException;
import com.fullstack.chatapp.models.Conversation;
import com.fullstack.chatapp.models.ConversationParticipant;
import com.fullstack.chatapp.models.Message;
import com.fullstack.chatapp.models.MessageStatus;
import com.fullstack.chatapp.models.User;
import com.fullstack.chatapp.repositories.ConversationRepository;
import com.fullstack.chatapp.repositories.MessageRepository;
import com.fullstack.chatapp.repositories.UserRepository;
import com.fullstack.chatapp.services.MessageBroadcast;
import com.fullstack.chatapp.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private static final int PREVIEW_LENGTH = 200;

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Override
    @Transactional
    public MessageBroadcast sendMessage(String senderUsername, SendMessageRequestDto request) {
        User sender = requireUser(senderUsername);
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + request.getConversationId()));

        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(participant -> participant.getUser().getId().equals(sender.getId()));
        if (!isParticipant) {
            throw new UnauthorizedConversationException("You are not a participant in this conversation");
        }

        String content = request.getContent() == null ? "" : request.getContent().trim();
        if (content.isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }

        LocalDateTime now = LocalDateTime.now();
        Message message = messageRepository.save(Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content)
                .sentAt(now)
                .status(MessageStatus.SENT)
                .build());

        conversation.setUpdatedAt(now);
        conversation.setLastMessageAt(now);
        conversation.setLastMessagePreview(content.substring(0, Math.min(content.length(), PREVIEW_LENGTH)));

        User recipient = conversation.getParticipants().stream()
                .map(ConversationParticipant::getUser)
                .filter(user -> !user.getId().equals(sender.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Conversation has no other participant"));

        return new MessageBroadcast(toDto(message), sender.getUsername(), recipient.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<MessageResponseDto> getMessages(Long conversationId, String username, int page, int size) {
        User me = requireUser(username);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));
        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(participant -> participant.getUser().getId().equals(me.getId()));
        if (!isParticipant) {
            throw new UnauthorizedConversationException("You are not a participant in this conversation");
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Page<Message> messages = messageRepository.findByConversationIdOrderBySentAtDesc(
                conversationId, PageRequest.of(safePage, safeSize, Sort.by("sentAt").descending()));

        return PageResponseDto.<MessageResponseDto>builder()
                .content(messages.getContent().stream().map(this::toDto).toList())
                .page(messages.getNumber())
                .size(messages.getSize())
                .totalElements(messages.getTotalElements())
                .totalPages(messages.getTotalPages())
                .last(messages.isLast())
                .build();
    }

    private User requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private MessageResponseDto toDto(Message message) {
        return MessageResponseDto.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .status(message.getStatus())
                .build();
    }
}
