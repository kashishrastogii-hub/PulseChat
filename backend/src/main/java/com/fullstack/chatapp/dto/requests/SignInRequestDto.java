package com.fullstack.chatapp.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignInRequestDto {
    /** Username or email address. */
    @NotBlank
    private String identifier;

    @NotBlank
    private String password;
}
