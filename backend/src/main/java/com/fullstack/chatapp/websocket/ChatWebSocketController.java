package com.fullstack.chatapp.websocket;

import com.fullstack.chatapp.dto.requests.ReadReceiptRequestDto;
import com.fullstack.chatapp.dto.requests.SendMessageRequestDto;
import com.fullstack.chatapp.services.ChatNotifier;
import com.fullstack.chatapp.services.ConversationService;
import com.fullstack.chatapp.services.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {
    private final MessageService messageService;
    private final ConversationService conversationService;
    private final ChatNotifier chatNotifier;

    /** Client sends to /app/chat.send; persisted messages go out on /user/queue/messages. */
    @MessageMapping("/chat.send")
    public void send(SendMessageRequestDto request, Principal principal) {
        try {
            chatNotifier.notifyNewMessage(messageService.sendMessage(principal.getName(), request));
        } catch (RuntimeException ex) {
            log.warn("Rejected WebSocket message from {}: {}", principal.getName(), ex.getMessage());
            chatNotifier.notifyError(principal.getName(), ex.getMessage());
        }
    }

    /** Client sends to /app/chat.read; the other participant receives /user/queue/read. */
    @MessageMapping("/chat.read")
    public void read(ReadReceiptRequestDto request, Principal principal) {
        if (request == null || request.getConversationId() == null) {
            return;
        }
        try {
            String username = principal.getName();
            conversationService.markAsRead(request.getConversationId(), username);
            String otherUsername = conversationService.getOtherParticipantUsername(request.getConversationId(), username);
            chatNotifier.notifyRead(request.getConversationId(), username, otherUsername);
        } catch (RuntimeException ex) {
            log.warn("Rejected read receipt from {}: {}", principal.getName(), ex.getMessage());
        }
    }
}
