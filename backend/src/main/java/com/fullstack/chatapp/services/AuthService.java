package com.fullstack.chatapp.services;

import com.fullstack.chatapp.dto.requests.SignInRequestDto;
import com.fullstack.chatapp.dto.requests.SignUpRequestDto;
import com.fullstack.chatapp.dto.responses.JwtResponseDto;

public interface AuthService {
    JwtResponseDto signUp(SignUpRequestDto request);

    JwtResponseDto signIn(SignInRequestDto request);
}
