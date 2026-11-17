package com.fullstack.chatapp.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/** Body for the REST fallback endpoint - the conversation id comes from the URL. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageContentRequestDto {
    @NotBlank
    @Size(max = 2000)
    private String content;
}
