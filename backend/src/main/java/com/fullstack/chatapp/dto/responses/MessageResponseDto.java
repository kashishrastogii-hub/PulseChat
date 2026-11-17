package com.fullstack.chatapp.dto.responses;

import com.fullstack.chatapp.models.MessageStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDto {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderUsername;
    private String content;
    private LocalDateTime sentAt;
    private MessageStatus status;
}
