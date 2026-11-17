package com.fullstack.chatapp.controllers;

import com.fullstack.chatapp.dto.requests.MessageContentRequestDto;
import com.fullstack.chatapp.dto.requests.SendMessageRequestDto;
import com.fullstack.chatapp.dto.responses.ApiResponseDto;
import com.fullstack.chatapp.dto.responses.MessageResponseDto;
import com.fullstack.chatapp.dto.responses.PageResponseDto;
import com.fullstack.chatapp.security.SecurityUtils;
import com.fullstack.chatapp.services.ChatNotifier;
import com.fullstack.chatapp.services.MessageBroadcast;
import com.fullstack.chatapp.services.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST fallback for clients that cannot hold a WebSocket connection (also handy for Postman). */
@RestController
@RequestMapping("/api/conversations/{conversationId}/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;
    private final ChatNotifier chatNotifier;

    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<MessageResponseDto>>> list(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponseDto.ok(
                messageService.getMessages(conversationId, SecurityUtils.currentUsername(), page, size)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<MessageResponseDto>> send(
            @PathVariable Long conversationId,
            @Valid @RequestBody MessageContentRequestDto request) {
        SendMessageRequestDto messageRequest = SendMessageRequestDto.builder()
                .conversationId(conversationId)
                .content(request.getContent())
                .build();
        MessageBroadcast broadcast = messageService.sendMessage(SecurityUtils.currentUsername(), messageRequest);
        chatNotifier.notifyNewMessage(broadcast);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.ok(broadcast.message(), "Message sent"));
    }
}
