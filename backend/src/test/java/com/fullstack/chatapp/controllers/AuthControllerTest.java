package com.fullstack.chatapp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fullstack.chatapp.dto.requests.SignInRequestDto;
import com.fullstack.chatapp.dto.requests.SignUpRequestDto;
import com.fullstack.chatapp.dto.responses.JwtResponseDto;
import com.fullstack.chatapp.exceptions.GlobalExceptionHandler;
import com.fullstack.chatapp.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void signUpReturnsJwt() throws Exception {
        when(authService.signUp(any(SignUpRequestDto.class))).thenReturn(JwtResponseDto.builder()
                .token("token").id(1L).username("alice").email("alice@example.com").build());

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(SignUpRequestDto.builder()
                                .username("alice").email("alice@example.com").password("secret123").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("token"));
    }

    @Test
    void signUpRejectsInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"a\",\"email\":\"not-an-email\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.email").exists());

        verifyNoInteractions(authService);
    }

    @Test
    void signInReturnsJwt() throws Exception {
        when(authService.signIn(any(SignInRequestDto.class))).thenReturn(JwtResponseDto.builder()
                .token("login-token").username("bob").build());

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(SignInRequestDto.builder()
                                .identifier("bob").password("secret123").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("login-token"));
    }
}
