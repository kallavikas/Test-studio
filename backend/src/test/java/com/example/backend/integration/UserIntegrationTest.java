package com.example.backend.integration;

import com.example.backend.dto.AuthDto;
import com.example.backend.dto.UserDto;
import com.example.backend.entity.Role;
import com.example.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up database
        userRepository.deleteAll();
        
        // Register and login to get JWT token for authenticated requests
        AuthDto.RegisterRequest registerRequest = new AuthDto.RegisterRequest(
                "testuser",
                "test@example.com",
                "password123",
                "Test",
                "User"
        );

        // Register user
        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login to get token
        AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest("testuser", "password123");
        
        String loginResponse = mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract token from response (simplified - in real test you'd parse JSON properly)
        // For this test, we'll use a mock token
        jwtToken = "Bearer mock-jwt-token";
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_Success() throws Exception {
        // Given
        UserDto newUser = new UserDto();
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setEnabled(true);
        newUser.setRoles(Set.of(Role.USER));

        // When & Then
        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andExpect(jsonPath("$.data.email").value("newuser@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pagination").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchUsers_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/users/search")
                        .param("q", "test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void checkUsernameAvailability_Available() throws Exception {
        // When & Then
        mockMvc.perform(get("/users/check/username/availableuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.available").value(true))
                .andExpect(jsonPath("$.data.exists").value(false));
    }

    @Test
    void checkUsernameAvailability_NotAvailable() throws Exception {
        // When & Then
        mockMvc.perform(get("/users/check/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.available").value(false))
                .andExpect(jsonPath("$.data.exists").value(true));
    }

    @Test
    void checkEmailAvailability_Available() throws Exception {
        // When & Then
        mockMvc.perform(get("/users/check/email/available@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.available").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createUser_InsufficientPermissions_Forbidden() throws Exception {
        // Given
        UserDto newUser = new UserDto();
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");

        // When & Then
        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_Unauthenticated_Unauthorized() throws Exception {
        // Given
        UserDto newUser = new UserDto();
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");

        // When & Then
        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isUnauthorized());
    }
}
