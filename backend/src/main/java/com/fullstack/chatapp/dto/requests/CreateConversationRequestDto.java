package com.fullstack.chatapp.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateConversationRequestDto {
    @NotBlank
    private String participantUsername;
}
