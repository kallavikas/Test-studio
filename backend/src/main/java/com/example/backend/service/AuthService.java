package com.example.backend.service;

import com.example.backend.dto.AuthDto;
import com.example.backend.dto.UserDto;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.exception.AuthenticationException;
import com.example.backend.exception.DuplicateResourceException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Service class for Authentication operations
 */
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                      AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Register a new user
     * @param registerRequest the registration request
     * @return user DTO
     */
    public UserDto register(AuthDto.RegisterRequest registerRequest) {
        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + registerRequest.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + registerRequest.getEmail());
        }

        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        
        // Set default role
        Set<Role> roles = new HashSet<>();
        roles.add(Role.USER);
        user.setRoles(roles);
        
        // Set default account status
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    /**
     * Authenticate user and generate JWT token
     * @param loginRequest the login request
     * @return JWT response with token and user details
     */
    public AuthDto.JwtResponse login(AuthDto.LoginRequest loginRequest) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsernameOrEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user details
            String usernameOrEmail = loginRequest.getUsernameOrEmail();
            User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + usernameOrEmail));

            // Generate tokens
            String accessToken = jwtTokenProvider.generateAccessToken(user);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user);
            Long expiresIn = jwtTokenProvider.getAccessTokenExpirationTime() / 1000; // Convert to seconds

            return new AuthDto.JwtResponse(
                    accessToken,
                    refreshToken,
                    convertToDto(user),
                    user.getRoles(),
                    expiresIn
            );
        } catch (Exception e) {
            throw new AuthenticationException("Invalid username/email or password");
        }
    }

    /**
     * Refresh JWT token
     * @param refreshToken the refresh token
     * @return new JWT response with refreshed token
     */
    public AuthDto.JwtResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new AuthenticationException("Invalid or expired refresh token");
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Generate new tokens
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);
        Long expiresIn = jwtTokenProvider.getAccessTokenExpirationTime() / 1000; // Convert to seconds

        return new AuthDto.JwtResponse(
                newAccessToken,
                newRefreshToken,
                convertToDto(user),
                user.getRoles(),
                expiresIn
        );
    }

    /**
     * Logout user by invalidating token
     * @param token the JWT token to invalidate
     */
    public void logout(String token) {
        // In a production system, you would typically:
        // 1. Add the token to a blacklist/cache with expiration
        // 2. Store invalidated tokens in Redis or database
        // 3. Check blacklist during token validation
        
        // For this implementation, we'll add the token to a blacklist
        jwtTokenProvider.invalidateToken(token);
        
        // Clear security context
        SecurityContextHolder.clearContext();
    }

    /**
     * Change user password
     * @param token the JWT token
     * @param changePasswordRequest the password change request
     */
    public void changePassword(String token, AuthDto.ChangePasswordRequest changePasswordRequest) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new AuthenticationException("Invalid or expired token");
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Verify current password
        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Validate JWT token
     * @param token the JWT token
     * @return true if token is valid
     */
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    /**
     * Get current authenticated user
     * @return current user DTO
     */
    @Transactional(readOnly = true)
    public UserDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("No authenticated user found");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        return convertToDto(user);
    }

    /**
     * Check if user has specific role
     * @param username the username
     * @param role the role to check
     * @return true if user has the role
     */
    @Transactional(readOnly = true)
    public boolean hasRole(String username, Role role) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return user.getRoles().contains(role);
    }

    /**
     * Check if user account is enabled and not locked
     * @param username the username
     * @return true if account is active
     */
    @Transactional(readOnly = true)
    public boolean isAccountActive(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        return user.getEnabled() && 
               user.getAccountNonExpired() && 
               user.getAccountNonLocked() && 
               user.getCredentialsNonExpired();
    }

    // Helper method for entity-DTO conversion
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
}
