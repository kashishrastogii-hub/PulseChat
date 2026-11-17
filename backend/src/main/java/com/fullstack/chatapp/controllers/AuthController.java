package com.fullstack.chatapp.controllers;

import com.fullstack.chatapp.dto.requests.SignInRequestDto;
import com.fullstack.chatapp.dto.requests.SignUpRequestDto;
import com.fullstack.chatapp.dto.responses.ApiResponseDto;
import com.fullstack.chatapp.dto.responses.JwtResponseDto;
import com.fullstack.chatapp.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDto<JwtResponseDto>> signUp(@Valid @RequestBody SignUpRequestDto request) {
        return ResponseEntity.ok(ApiResponseDto.ok(authService.signUp(request), "Account created"));
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponseDto<JwtResponseDto>> signIn(@Valid @RequestBody SignInRequestDto request) {
        return ResponseEntity.ok(ApiResponseDto.ok(authService.signIn(request), "Signed in"));
    }
}
