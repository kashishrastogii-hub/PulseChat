package com.fullstack.chatapp.services;

import com.fullstack.chatapp.dto.responses.UserResponseDto;

import java.util.List;

public interface UserService {
    UserResponseDto getCurrentUser(String username);

    List<UserResponseDto> searchUsers(String query, String currentUsername);
}
