package com.example.backend.service;

import com.example.backend.dto.UserDto;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.exception.DuplicateResourceException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDto testUserDto;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        
        testUser = new User();
        testUser.setId(testUserId);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEnabled(true);
        testUser.setRoles(Set.of(Role.USER));
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        testUserDto = new UserDto();
        testUserDto.setId(testUserId);
        testUserDto.setUsername("testuser");
        testUserDto.setEmail("test@example.com");
        testUserDto.setFirstName("Test");
        testUserDto.setLastName("User");
        testUserDto.setEnabled(true);
        testUserDto.setRoles(Set.of(Role.USER));
    }

    @Test
    void createUser_Success() {
        // Given
        when(userRepository.existsByUsername(testUserDto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(testUserDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDto result = userService.createUser(testUserDto);

        // Then
        assertNotNull(result);
        assertEquals(testUserDto.getUsername(), result.getUsername());
        assertEquals(testUserDto.getEmail(), result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_DuplicateUsername_ThrowsException() {
        // Given
        when(userRepository.existsByUsername(testUserDto.getUsername())).thenReturn(true);

        // When & Then
        assertThrows(DuplicateResourceException.class, () -> {
            userService.createUser(testUserDto);
        });
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_DuplicateEmail_ThrowsException() {
        // Given
        when(userRepository.existsByUsername(testUserDto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(testUserDto.getEmail())).thenReturn(true);

        // When & Then
        assertThrows(DuplicateResourceException.class, () -> {
            userService.createUser(testUserDto);
        });
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When
        UserDto result = userService.getUserById(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(testUserId);
        });
    }

    @Test
    void getUserByUsername_Success() {
        // Given
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

        // When
        UserDto result = userService.getUserByUsername(testUser.getUsername());

        // Then
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
    }

    @Test
    void getAllUsers_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users, pageable, 1);
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // When
        Page<UserDto> result = userService.getAllUsers(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testUser.getUsername(), result.getContent().get(0).getUsername());
    }

    @Test
    void updateUser_Success() {
        // Given
        UserDto updateDto = new UserDto();
        updateDto.setUsername("updateduser");
        updateDto.setEmail("updated@example.com");
        updateDto.setFirstName("Updated");
        updateDto.setLastName("User");
        updateDto.setEnabled(true);
        
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername(updateDto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(updateDto.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDto result = userService.updateUser(testUserId, updateDto);

        // Then
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_Success() {
        // Given
        when(userRepository.existsById(testUserId)).thenReturn(true);

        // When
        userService.deleteUser(testUserId);

        // Then
        verify(userRepository).deleteById(testUserId);
    }

    @Test
    void deleteUser_NotFound_ThrowsException() {
        // Given
        when(userRepository.existsById(testUserId)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(testUserId);
        });
        
        verify(userRepository, never()).deleteById(testUserId);
    }

    @Test
    void updateUserStatus_Success() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDto result = userService.updateUserStatus(testUserId, false);

        // Then
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void addRoleToUser_Success() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDto result = userService.addRoleToUser(testUserId, Role.ADMIN);

        // Then
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void existsByUsername_ReturnsTrue() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When
        boolean result = userService.existsByUsername("testuser");

        // Then
        assertTrue(result);
    }

    @Test
    void existsByEmail_ReturnsFalse() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);

        // When
        boolean result = userService.existsByEmail("test@example.com");

        // Then
        assertFalse(result);
    }
}
