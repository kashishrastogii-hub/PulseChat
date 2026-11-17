package com.fullstack.chatapp.services;

import com.fullstack.chatapp.dto.responses.ConversationResponseDto;

import java.util.List;

public interface ConversationService {
    ConversationResponseDto createOrGetDirectConversation(String currentUsername, String otherUsername);

    List<ConversationResponseDto> getConversations(String username);

    ConversationResponseDto getConversation(Long conversationId, String username);

    void markAsRead(Long conversationId, String username);

    String getOtherParticipantUsername(Long conversationId, String username);
}
