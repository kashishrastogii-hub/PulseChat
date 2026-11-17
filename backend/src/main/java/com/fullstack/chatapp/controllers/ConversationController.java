package com.fullstack.chatapp.controllers;

import com.fullstack.chatapp.dto.requests.CreateConversationRequestDto;
import com.fullstack.chatapp.dto.responses.ApiResponseDto;
import com.fullstack.chatapp.dto.responses.ConversationResponseDto;
import com.fullstack.chatapp.security.SecurityUtils;
import com.fullstack.chatapp.services.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {
    private final ConversationService conversationService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<ConversationResponseDto>> create(
            @Valid @RequestBody CreateConversationRequestDto request) {
        return ResponseEntity.ok(ApiResponseDto.ok(
                conversationService.createOrGetDirectConversation(
                        SecurityUtils.currentUsername(), request.getParticipantUsername()),
                "Conversation ready"));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<ConversationResponseDto>>> list() {
        return ResponseEntity.ok(ApiResponseDto.ok(
                conversationService.getConversations(SecurityUtils.currentUsername())));
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ApiResponseDto<ConversationResponseDto>> get(@PathVariable Long conversationId) {
        return ResponseEntity.ok(ApiResponseDto.ok(
                conversationService.getConversation(conversationId, SecurityUtils.currentUsername())));
    }

    @PostMapping("/{conversationId}/read")
    public ResponseEntity<ApiResponseDto<Void>> markAsRead(@PathVariable Long conversationId) {
        conversationService.markAsRead(conversationId, SecurityUtils.currentUsername());
        return ResponseEntity.ok(ApiResponseDto.ok(null, "Conversation marked as read"));
    }
}
