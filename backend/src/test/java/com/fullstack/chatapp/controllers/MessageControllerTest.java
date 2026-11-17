package com.fullstack.chatapp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fullstack.chatapp.dto.requests.MessageContentRequestDto;
import com.fullstack.chatapp.dto.requests.SendMessageRequestDto;
import com.fullstack.chatapp.dto.responses.MessageResponseDto;
import com.fullstack.chatapp.dto.responses.PageResponseDto;
import com.fullstack.chatapp.exceptions.GlobalExceptionHandler;
import com.fullstack.chatapp.models.MessageStatus;
import com.fullstack.chatapp.security.SecurityUtils;
import com.fullstack.chatapp.services.ChatNotifier;
import com.fullstack.chatapp.services.MessageBroadcast;
import com.fullstack.chatapp.services.MessageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MessageControllerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private MessageService messageService;
    private ChatNotifier chatNotifier;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice", null, List.of()));
        messageService = mock(MessageService.class);
        chatNotifier = mock(ChatNotifier.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new MessageController(messageService, chatNotifier))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listsMessages() throws Exception {
        when(messageService.getMessages(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(PageResponseDto.<MessageResponseDto>builder()
                        .content(List.of(MessageResponseDto.builder().id(1L).content("hi").build()))
                        .page(0).size(20).totalElements(1).totalPages(1).last(true).build());

        mockMvc.perform(get("/api/conversations/5/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].content").value("hi"));
    }

    @Test
    void sendsMessageAndBroadcasts() throws Exception {
        MessageResponseDto saved = MessageResponseDto.builder()
                .id(9L).conversationId(5L).senderId(1L).senderUsername("alice")
                .content("hello").sentAt(LocalDateTime.now()).status(MessageStatus.SENT).build();
        when(messageService.sendMessage(eq("alice"), any(SendMessageRequestDto.class)))
                .thenReturn(new MessageBroadcast(saved, "alice", "bob"));

        mockMvc.perform(post("/api/conversations/5/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(MessageContentRequestDto.builder()
                                .content("hello").build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(9));

        verify(chatNotifier).notifyNewMessage(any(MessageBroadcast.class));
    }
}
