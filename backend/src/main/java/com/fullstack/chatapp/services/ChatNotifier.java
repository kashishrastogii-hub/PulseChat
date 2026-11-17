package com.fullstack.chatapp.services;

public interface ChatNotifier {
    void notifyNewMessage(MessageBroadcast broadcast);

    void notifyRead(long conversationId, String readerUsername, String recipientUsername);

    void notifyError(String username, String message);
}
