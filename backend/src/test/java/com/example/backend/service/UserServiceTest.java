package com.example.backend.service;

import com.example.backend.entity.User;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setActive(true);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getAllUsers_ShouldReturnPagedUsers() {
        // Given
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());
        when(userRepository.findByActiveTrue(pageable)).thenReturn(userPage);

        // When
        Page<User> result = userService.getAllUsers(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testUser.getUsername(), result.getContent().get(0).getUsername());
        verify(userRepository).findByActiveTrue(pageable);
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.getUserById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getUsername(), result.get().getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_WhenUserNotExists_ShouldReturnEmpty() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserById(1L);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_WhenUserInactive_ShouldReturnEmpty() {
        // Given
        testUser.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.getUserById(1L);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findById(1L);
    }

    @Test
    void createUser_ShouldEncodePasswordAndSaveUser() {
        // Given
        String rawPassword = "rawPassword";
        String encodedPassword = "encodedPassword";
        testUser.setPassword(rawPassword);
        
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.createUser(testUser);

        // Then
        assertNotNull(result);
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(testUser);
        assertEquals(encodedPassword, testUser.getPassword());
    }

    @Test
    void updateUser_WhenUserExists_ShouldUpdateAndReturnUser() {
        // Given
        User updateDetails = new User();
        updateDetails.setUsername("updateduser");
        updateDetails.setEmail("updated@example.com");
        updateDetails.setPassword("newpassword");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        Optional<User> result = userService.updateUser(1L, updateDetails);

        // Then
        assertTrue(result.isPresent());
        assertEquals("updateduser", testUser.getUsername());
        assertEquals("updated@example.com", testUser.getEmail());
        verify(passwordEncoder).encode("newpassword");
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_WhenUserNotExists_ShouldReturnEmpty() {
        // Given
        User updateDetails = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.updateUser(1L, updateDetails);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_WhenUserExists_ShouldMarkAsInactiveAndReturnTrue() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        boolean result = userService.deleteUser(1L);

        // Then
        assertTrue(result);
        assertFalse(testUser.getActive());
        verify(userRepository).save(testUser);
    }

    @Test
    void deleteUser_WhenUserNotExists_ShouldReturnFalse() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        boolean result = userService.deleteUser(1L);

        // Then
        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void existsByUsername_ShouldReturnRepositoryResult() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When
        boolean result = userService.existsByUsername("testuser");

        // Then
        assertTrue(result);
        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    void existsByEmail_ShouldReturnRepositoryResult() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When
        boolean result = userService.existsByEmail("test@example.com");

        // Then
        assertTrue(result);
        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    void searchUsers_WithSearchTerm_ShouldCallSearchMethod() {
        // Given
        String searchTerm = "test";
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());
        when(userRepository.findActiveUsersBySearch(searchTerm, pageable)).thenReturn(userPage);

        // When
        Page<User> result = userService.searchUsers(searchTerm, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userRepository).findActiveUsersBySearch(searchTerm, pageable);
        verify(userRepository, never()).findByActiveTrue(pageable);
    }

    @Test
    void searchUsers_WithEmptySearchTerm_ShouldCallGetAllUsers() {
        // Given
        String searchTerm = "";
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());
        when(userRepository.findByActiveTrue(pageable)).thenReturn(userPage);

        // When
        Page<User> result = userService.searchUsers(searchTerm, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userRepository).findByActiveTrue(pageable);
        verify(userRepository, never()).findActiveUsersBySearch(anyString(), any(Pageable.class));
    }
}