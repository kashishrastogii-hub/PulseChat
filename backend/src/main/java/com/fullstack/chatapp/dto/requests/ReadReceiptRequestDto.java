package com.fullstack.chatapp.dto.requests;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadReceiptRequestDto {
    private Long conversationId;
}
