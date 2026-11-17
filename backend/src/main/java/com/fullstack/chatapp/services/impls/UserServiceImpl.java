package com.fullstack.chatapp.services.impls;

import com.fullstack.chatapp.dto.responses.UserResponseDto;
import com.fullstack.chatapp.exceptions.ResourceNotFoundException;
import com.fullstack.chatapp.models.User;
import com.fullstack.chatapp.repositories.UserRepository;
import com.fullstack.chatapp.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUser(String username) {
        return toDto(userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> searchUsers(String query, String currentUsername) {
        User current = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUsername));
        String term = query == null ? "" : query.trim();
        return userRepository.searchUsers(term, current.getId(), PageRequest.of(0, 20))
                .stream()
                .map(this::toDto)
                .toList();
    }

    private UserResponseDto toDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}
