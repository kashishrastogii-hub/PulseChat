package com.fullstack.chatapp.dto.responses;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDto<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponseDto<T> ok(T data) {
        return new ApiResponseDto<>(true, null, data);
    }

    public static <T> ApiResponseDto<T> ok(T data, String message) {
        return new ApiResponseDto<>(true, message, data);
    }

    public static <T> ApiResponseDto<T> fail(String message) {
        return new ApiResponseDto<>(false, message, null);
    }
}
