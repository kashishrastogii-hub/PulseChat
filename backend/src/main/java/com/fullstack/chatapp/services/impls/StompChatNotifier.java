package com.fullstack.chatapp.services.impls;

import com.fullstack.chatapp.services.ChatNotifier;
import com.fullstack.chatapp.services.MessageBroadcast;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/** Publishes persisted messages and read receipts to per-user STOMP queues. */
@Component
@RequiredArgsConstructor
public class StompChatNotifier implements ChatNotifier {
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void notifyNewMessage(MessageBroadcast broadcast) {
        // Recipient gets the live message; sender receives the persisted copy (with its id/timestamp)
        // so every browser tab shows exactly what was stored.
        messagingTemplate.convertAndSendToUser(broadcast.recipientUsername(), "/queue/messages", broadcast.message());
        messagingTemplate.convertAndSendToUser(broadcast.senderUsername(), "/queue/messages", broadcast.message());
    }

    @Override
    public void notifyRead(long conversationId, String readerUsername, String recipientUsername) {
        Map<String, Object> receipt = Map.of(
                "conversationId", conversationId,
                "readBy", readerUsername,
                "readAt", LocalDateTime.now().toString()
        );
        messagingTemplate.convertAndSendToUser(recipientUsername, "/queue/read", receipt);
    }

    @Override
    public void notifyError(String username, String message) {
        messagingTemplate.convertAndSendToUser(username, "/queue/errors", Map.of("message", message == null ? "Unknown error" : message));
    }
}
