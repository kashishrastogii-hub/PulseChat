package com.fullstack.chatapp.services.impls;

import com.fullstack.chatapp.dto.requests.SignInRequestDto;
import com.fullstack.chatapp.dto.requests.SignUpRequestDto;
import com.fullstack.chatapp.dto.responses.JwtResponseDto;
import com.fullstack.chatapp.exceptions.UserAlreadyExistsException;
import com.fullstack.chatapp.models.ERole;
import com.fullstack.chatapp.models.Role;
import com.fullstack.chatapp.models.User;
import com.fullstack.chatapp.repositories.RoleRepository;
import com.fullstack.chatapp.repositories.UserRepository;
import com.fullstack.chatapp.security.UserDetailsImpl;
import com.fullstack.chatapp.security.jwt.JwtUtils;
import com.fullstack.chatapp.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public JwtResponseDto signUp(SignUpRequestDto request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username is already taken: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email is already registered: " + email);
        }

        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("ROLE_USER has not been seeded"));

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .roles(new HashSet<>(java.util.Set.of(userRole)))
                .build();
        userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword()));
        return toResponse(authentication);
    }

    @Override
    public JwtResponseDto signIn(SignInRequestDto request) {
        String identifier = request.getIdentifier().trim();
        String username = userRepository.findByEmail(identifier.toLowerCase())
                .map(User::getUsername)
                .orElse(identifier);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword()));
        return toResponse(authentication);
    }

    private JwtResponseDto toResponse(Authentication authentication) {
        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        return JwtResponseDto.builder()
                .token(jwtUtils.generateJwtToken(authentication))
                .id(principal.getId())
                .username(principal.getUsername())
                .email(principal.getEmail())
                .build();
    }
}
