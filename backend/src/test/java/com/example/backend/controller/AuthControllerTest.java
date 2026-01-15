package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.AuthDto;
import com.example.backend.dto.UserDto;
import com.example.backend.entity.Role;
import com.example.backend.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthDto.RegisterRequest registerRequest;
    private AuthDto.LoginRequest loginRequest;
    private UserDto userDto;
    private AuthDto.JwtResponse jwtResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new AuthDto.RegisterRequest(
                "testuser",
                "test@example.com",
                "password123",
                "Test",
                "User"
        );

        loginRequest = new AuthDto.LoginRequest("testuser", "password123");

        userDto = new UserDto(
                UUID.randomUUID(),
                "testuser",
                "test@example.com",
                "Test",
                "User",
                true,
                Set.of(Role.USER),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        jwtResponse = new AuthDto.JwtResponse(
                "access-token",
                "refresh-token",
                userDto,
                Set.of(Role.USER),
                3600L
        );
    }

    @Test
    void register_Success() throws Exception {
        // Given
        when(authService.register(any(AuthDto.RegisterRequest.class))).thenReturn(userDto);

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    void register_InvalidInput_BadRequest() throws Exception {
        // Given - invalid request with missing required fields
        AuthDto.RegisterRequest invalidRequest = new AuthDto.RegisterRequest(
                "", // empty username
                "invalid-email", // invalid email format
                "123", // password too short
                null,
                null
        );

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpected(status().isBadRequest());
    }

    @Test
    void login_Success() throws Exception {
        // Given
        when(authService.login(any(AuthDto.LoginRequest.class))).thenReturn(jwtResponse);

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.user.username").value("testuser"));
    }

    @Test
    void login_InvalidCredentials_Unauthorized() throws Exception {
        // Given
        when(authService.login(any(AuthDto.LoginRequest.class)))
                .thenThrow(new com.example.backend.exception.AuthenticationException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_ERROR"));
    }

    @Test
    void refreshToken_Success() throws Exception {
        // Given
        AuthDto.RefreshTokenRequest refreshRequest = new AuthDto.RefreshTokenRequest("refresh-token");
        when(authService.refreshToken("refresh-token")).thenReturn(jwtResponse);

        // When & Then
        mockMvc.perform(post("/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                .andExpect(jsonPath("$.data.token").value("access-token"));
    }

    @Test
    @WithMockUser
    void logout_Success() throws Exception {
        // When & Then
        mockMvc.perform(post("/auth/logout")
                        .with(csrf())
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }

    @Test
    @WithMockUser
    void changePassword_Success() throws Exception {
        // Given
        AuthDto.ChangePasswordRequest changePasswordRequest = new AuthDto.ChangePasswordRequest(
                "currentPassword",
                "newPassword123"
        );

        // When & Then
        mockMvc.perform(post("/auth/change-password")
                        .with(csrf())
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }

    @Test
    @WithMockUser
    void validateToken_Success() throws Exception {
        // Given
        when(authService.validateToken("access-token")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/auth/validate")
                        .with(csrf())
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.valid").value(true));
    }
}
