package com.fullstack.chatapp.exceptions;

public class UnauthorizedConversationException extends RuntimeException {
    public UnauthorizedConversationException(String message) {
        super(message);
    }
}
