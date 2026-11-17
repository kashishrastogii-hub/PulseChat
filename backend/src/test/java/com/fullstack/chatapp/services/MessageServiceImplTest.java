package com.fullstack.chatapp.services;

import com.fullstack.chatapp.dto.requests.SendMessageRequestDto;
import com.fullstack.chatapp.dto.responses.PageResponseDto;
import com.fullstack.chatapp.exceptions.UnauthorizedConversationException;
import com.fullstack.chatapp.models.Conversation;
import com.fullstack.chatapp.models.ConversationParticipant;
import com.fullstack.chatapp.models.Message;
import com.fullstack.chatapp.models.MessageStatus;
import com.fullstack.chatapp.models.User;
import com.fullstack.chatapp.repositories.ConversationRepository;
import com.fullstack.chatapp.repositories.MessageRepository;
import com.fullstack.chatapp.repositories.UserRepository;
import com.fullstack.chatapp.services.impls.MessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {
    @Mock private UserRepository userRepository;
    @Mock private ConversationRepository conversationRepository;
    @Mock private MessageRepository messageRepository;

    private MessageServiceImpl service;
    private User alice;
    private User bob;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        service = new MessageServiceImpl(userRepository, conversationRepository, messageRepository);
        alice = User.builder().id(1L).username("alice").build();
        bob = User.builder().id(2L).username("bob").build();
        LocalDateTime now = LocalDateTime.now();
        conversation = Conversation.builder()
                .id(5L).createdAt(now).updatedAt(now).lastMessageAt(now)
                .participants(new HashSet<>()).build();
        conversation.getParticipants().add(ConversationParticipant.builder()
                .conversation(conversation).user(alice).joinedAt(now).build());
        conversation.getParticipants().add(ConversationParticipant.builder()
                .conversation(conversation).user(bob).joinedAt(now).build());
    }

    @Test
    void sendMessagePersistsAndReturnsBroadcastRecipients() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(conversationRepository.findById(5L)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        MessageBroadcast broadcast = service.sendMessage("alice", SendMessageRequestDto.builder()
                .conversationId(5L).content("  hello bob  ").build());

        assertThat(broadcast.message().getId()).isEqualTo(100L);
        assertThat(broadcast.message().getContent()).isEqualTo("hello bob");
        assertThat(broadcast.senderUsername()).isEqualTo("alice");
        assertThat(broadcast.recipientUsername()).isEqualTo("bob");
        assertThat(conversation.getLastMessagePreview()).isEqualTo("hello bob");
        assertThat(conversation.getLastMessageAt()).isNotNull();
    }

    @Test
    void sendMessageRejectsNonParticipant() {
        User charlie = User.builder().id(3L).username("charlie").build();
        when(userRepository.findByUsername("charlie")).thenReturn(Optional.of(charlie));
        when(conversationRepository.findById(5L)).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> service.sendMessage("charlie", SendMessageRequestDto.builder()
                .conversationId(5L).content("hi").build()))
                .isInstanceOf(UnauthorizedConversationException.class);

        verify(messageRepository, never()).save(any());
    }

    @Test
    void getMessagesReturnsMappedPage() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(conversationRepository.findById(5L)).thenReturn(Optional.of(conversation));
        Message saved = Message.builder().id(7L).conversation(conversation).sender(bob)
                .content("hi").sentAt(LocalDateTime.now()).status(MessageStatus.SENT).build();
        when(messageRepository.findByConversationIdOrderBySentAtDesc(eq(5L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(saved), PageRequest.of(0, 20), 1));

        PageResponseDto<?> page = service.getMessages(5L, "alice", 0, 20);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.isLast()).isTrue();
    }
}
