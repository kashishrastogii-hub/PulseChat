package com.fullstack.chatapp.services;

import com.fullstack.chatapp.dto.requests.SendMessageRequestDto;
import com.fullstack.chatapp.dto.responses.PageResponseDto;
import com.fullstack.chatapp.dto.responses.MessageResponseDto;

public interface MessageService {
    MessageBroadcast sendMessage(String senderUsername, SendMessageRequestDto request);

    PageResponseDto<MessageResponseDto> getMessages(Long conversationId, String username, int page, int size);
}
