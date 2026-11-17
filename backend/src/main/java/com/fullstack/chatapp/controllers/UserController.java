package com.fullstack.chatapp.controllers;

import com.fullstack.chatapp.dto.responses.ApiResponseDto;
import com.fullstack.chatapp.dto.responses.UserResponseDto;
import com.fullstack.chatapp.security.SecurityUtils;
import com.fullstack.chatapp.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> me() {
        return ResponseEntity.ok(ApiResponseDto.ok(userService.getCurrentUser(SecurityUtils.currentUsername())));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponseDto<List<UserResponseDto>>> search(
            @RequestParam(defaultValue = "") String query) {
        return ResponseEntity.ok(ApiResponseDto.ok(
                userService.searchUsers(query, SecurityUtils.currentUsername())));
    }
}
