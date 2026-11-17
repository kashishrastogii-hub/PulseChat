package com.fullstack.chatapp.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequestDto {
    @NotNull
    private Long conversationId;

    @NotBlank
    @Size(max = 2000)
    private String content;
}
