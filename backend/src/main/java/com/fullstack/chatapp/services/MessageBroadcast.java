package com.fullstack.chatapp.services;

import com.fullstack.chatapp.dto.responses.MessageResponseDto;

/** The saved message plus the STOMP destinations it must be delivered to. */
public record MessageBroadcast(MessageResponseDto message, String senderUsername, String recipientUsername) {
}
