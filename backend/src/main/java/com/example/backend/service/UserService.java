package com.example.backend.service;

import com.example.backend.dto.UserDto;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.DuplicateResourceException;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for User entity operations
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create a new user
     * @param userDto the user data
     * @return created user DTO
     */
    @CacheEvict(value = "users", allEntries = true)
    public UserDto createUser(UserDto userDto) {
        // Check if username already exists
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + userDto.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + userDto.getEmail());
        }

        User user = convertToEntity(userDto);
        user.setPassword(passwordEncoder.encode("defaultPassword123")); // Set default password
        
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    /**
     * Get user by ID
     * @param id the user ID
     * @return user DTO
     */
    @Cacheable(value = "users", key = "#id")
    @Transactional(readOnly = true)
    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToDto(user);
    }

    /**
     * Get user by username
     * @param username the username
     * @return user DTO
     */
    @Cacheable(value = "users", key = "#username")
    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return convertToDto(user);
    }

    /**
     * Get user by email
     * @param email the email
     * @return user DTO
     */
    @Cacheable(value = "users", key = "#email")
    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return convertToDto(user);
    }

    /**
     * Get all users with pagination
     * @param pageable pagination information
     * @return page of user DTOs
     */
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::convertToDto);
    }

    /**
     * Search users by term
     * @param searchTerm the search term
     * @param pageable pagination information
     * @return page of user DTOs
     */
    @Transactional(readOnly = true)
    public Page<UserDto> searchUsers(String searchTerm, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(searchTerm, pageable);
        return users.map(this::convertToDto);
    }

    /**
     * Get users by role
     * @param role the role
     * @param pageable pagination information
     * @return page of user DTOs
     */
    @Transactional(readOnly = true)
    public Page<UserDto> getUsersByRole(Role role, Pageable pageable) {
        Page<User> users = userRepository.findByRolesContaining(role, pageable);
        return users.map(this::convertToDto);
    }

    /**
     * Update user
     * @param id the user ID
     * @param userDto the updated user data
     * @return updated user DTO
     */
    @CacheEvict(value = "users", key = "#id")
    public UserDto updateUser(UUID id, UserDto userDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Check if username is being changed and if it already exists
        if (!existingUser.getUsername().equals(userDto.getUsername()) &&
            userRepository.existsByUsername(userDto.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + userDto.getUsername());
        }

        // Check if email is being changed and if it already exists
        if (!existingUser.getEmail().equals(userDto.getEmail()) &&
            userRepository.existsByEmail(userDto.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + userDto.getEmail());
        }

        // Update fields
        existingUser.setUsername(userDto.getUsername());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setFirstName(userDto.getFirstName());
        existingUser.setLastName(userDto.getLastName());
        
        if (userDto.getEnabled() != null) {
            existingUser.setEnabled(userDto.getEnabled());
        }
        
        if (userDto.getRoles() != null) {
            existingUser.setRoles(userDto.getRoles());
        }

        User updatedUser = userRepository.save(existingUser);
        return convertToDto(updatedUser);
    }

    /**
     * Delete user
     * @param id the user ID
     */
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    /**
     * Enable/disable user
     * @param id the user ID
     * @param enabled the enabled status
     * @return updated user DTO
     */
    @CacheEvict(value = "users", key = "#id")
    public UserDto updateUserStatus(UUID id, boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        user.setEnabled(enabled);
        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    /**
     * Add role to user
     * @param id the user ID
     * @param role the role to add
     * @return updated user DTO
     */
    @CacheEvict(value = "users", key = "#id")
    public UserDto addRoleToUser(UUID id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        user.addRole(role);
        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    /**
     * Remove role from user
     * @param id the user ID
     * @param role the role to remove
     * @return updated user DTO
     */
    @CacheEvict(value = "users", key = "#id")
    public UserDto removeRoleFromUser(UUID id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        user.removeRole(role);
        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    /**
     * Change user password
     * @param id the user ID
     * @param newPassword the new password
     */
    @CacheEvict(value = "users", key = "#id")
    public void changePassword(UUID id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Get user count by role
     * @param role the role
     * @return count of users with the role
     */
    @Transactional(readOnly = true)
    public long getUserCountByRole(Role role) {
        return userRepository.countByRole(role);
    }

    /**
     * Check if username exists
     * @param username the username
     * @return true if exists
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Check if email exists
     * @param email the email
     * @return true if exists
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Helper methods for entity-DTO conversion
    private UserDto convertToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getEnabled(),
                user.getRoles(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private User convertToEntity(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        
        if (userDto.getEnabled() != null) {
            user.setEnabled(userDto.getEnabled());
        }
        
        if (userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
            user.setRoles(userDto.getRoles());
        }
        
        return user;
    }
}
