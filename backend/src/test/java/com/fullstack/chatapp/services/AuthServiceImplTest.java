package com.fullstack.chatapp.services;

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
import com.fullstack.chatapp.services.impls.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtils jwtUtils;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, roleRepository, passwordEncoder,
                authenticationManager, jwtUtils);
    }

    private Authentication authenticationFor(User user) {
        UserDetailsImpl principal = UserDetailsImpl.build(user);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Test
    void signUpCreatesUserAndReturnsJwt() {
        SignUpRequestDto request = SignUpRequestDto.builder()
                .username("alice").email("Alice@Example.com").password("secret123").build();

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(roleRepository.findByName(ERole.ROLE_USER))
                .thenReturn(Optional.of(Role.builder().id(1).name(ERole.ROLE_USER).build()));
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$encoded");

        User stored = User.builder().id(1L).username("alice").email("alice@example.com")
                .password("$2a$encoded").enabled(true).createdAt(LocalDateTime.now())
                .roles(Set.of(Role.builder().name(ERole.ROLE_USER).build())).build();
        when(userRepository.save(any(User.class))).thenReturn(stored);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authenticationFor(stored));
        when(jwtUtils.generateJwtToken(any(Authentication.class))).thenReturn("signed.jwt.token");

        JwtResponseDto response = authService.signUp(request);

        assertThat(response.getToken()).isEqualTo("signed.jwt.token");
        assertThat(response.getUsername()).isEqualTo("alice");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getId()).isEqualTo(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signUpRejectsDuplicateUsername() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> authService.signUp(SignUpRequestDto.builder()
                .username("alice").email("alice@example.com").password("secret123").build()))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username");

        verify(userRepository, never()).save(any());
    }

    @Test
    void signInAcceptsEmailAsIdentifier() {
        User stored = User.builder().id(2L).username("bob").email("bob@example.com")
                .password("hash").enabled(true).createdAt(LocalDateTime.now())
                .roles(Set.of(Role.builder().name(ERole.ROLE_USER).build())).build();

        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(stored));
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authenticationFor(stored));
        when(jwtUtils.generateJwtToken(any(Authentication.class))).thenReturn("bob-token");

        JwtResponseDto response = authService.signIn(SignInRequestDto.builder()
                .identifier("bob@example.com").password("secret123").build());

        assertThat(response.getUsername()).isEqualTo("bob");
        assertThat(response.getToken()).isEqualTo("bob-token");
    }
}
