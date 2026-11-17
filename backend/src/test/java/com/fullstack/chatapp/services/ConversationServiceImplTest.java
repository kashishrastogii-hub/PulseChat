package com.fullstack.chatapp.services;

import com.fullstack.chatapp.dto.responses.ConversationResponseDto;
import com.fullstack.chatapp.exceptions.ResourceNotFoundException;
import com.fullstack.chatapp.exceptions.UnauthorizedConversationException;
import com.fullstack.chatapp.models.Conversation;
import com.fullstack.chatapp.models.ConversationParticipant;
import com.fullstack.chatapp.models.User;
import com.fullstack.chatapp.repositories.ConversationRepository;
import com.fullstack.chatapp.repositories.MessageRepository;
import com.fullstack.chatapp.repositories.UserRepository;
import com.fullstack.chatapp.services.impls.ConversationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceImplTest {
    @Mock private UserRepository userRepository;
    @Mock private ConversationRepository conversationRepository;
    @Mock private MessageRepository messageRepository;

    private ConversationServiceImpl service;
    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        service = new ConversationServiceImpl(userRepository, conversationRepository, messageRepository);
        alice = User.builder().id(1L).username("alice").email("alice@example.com").build();
        bob = User.builder().id(2L).username("bob").email("bob@example.com").build();
    }

    private Conversation conversation(long id, User... participants) {
        LocalDateTime now = LocalDateTime.now();
        Conversation conversation = Conversation.builder()
                .id(id).createdAt(now).updatedAt(now).lastMessageAt(now)
                .lastMessagePreview("hello").participants(new HashSet<>()).build();
        for (User user : participants) {
            conversation.getParticipants().add(ConversationParticipant.builder()
                    .conversation(conversation).user(user).joinedAt(now).build());
        }
        return conversation;
    }

    @Test
    void reusesExistingDirectConversation() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));
        when(conversationRepository.findDirectConversation(1L, 2L))
                .thenReturn(Optional.of(conversation(10L, alice, bob)));

        ConversationResponseDto result = service.createOrGetDirectConversation("alice", "bob");

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getOtherUsername()).isEqualTo("bob");
        assertThat(result.getLastMessagePreview()).isEqualTo("hello");
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void createsConversationWhenNoneExists() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));
        when(conversationRepository.findDirectConversation(1L, 2L)).thenReturn(Optional.empty());
        when(conversationRepository.save(any(Conversation.class)))
                .thenAnswer(invocation -> {
                    Conversation saved = invocation.getArgument(0);
                    saved.setId(11L);
                    return saved;
                });

        ConversationResponseDto result = service.createOrGetDirectConversation("alice", "bob");

        assertThat(result.getId()).isEqualTo(11L);
        assertThat(result.getOtherUsername()).isEqualTo("bob");
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    void rejectsSelfConversation() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));

        assertThatThrownBy(() -> service.createOrGetDirectConversation("alice", "alice"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("yourself");
    }

    @Test
    void rejectsUnknownParticipant() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createOrGetDirectConversation("alice", "ghost"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsConversationOfAnotherUser() {
        User charlie = User.builder().id(3L).username("charlie").email("charlie@example.com").build();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(conversationRepository.findById(99L)).thenReturn(Optional.of(conversation(99L, bob, charlie)));

        assertThatThrownBy(() -> service.getConversation(99L, "alice"))
                .isInstanceOf(UnauthorizedConversationException.class);
    }
}
