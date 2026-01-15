package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.UserDto;
import com.example.backend.entity.Role;
import com.example.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for User management operations
 */
@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "APIs for managing users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Create a new user
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new user", description = "Creates a new user in the system")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    public ResponseEntity<ApiResponse<UserDto>> createUser(
            @Valid @RequestBody UserDto userDto) {
        UserDto createdUser = userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", createdUser));
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.getUserById(#id).username == authentication.name")
    @Operation(summary = "Get user by ID", description = "Retrieves a user by their ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<UserDto>> getUserById(
            @Parameter(description = "User ID") @PathVariable UUID id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * Get user by username
     */
    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.name")
    @Operation(summary = "Get user by username", description = "Retrieves a user by their username")
    public ResponseEntity<ApiResponse<UserDto>> getUserByUsername(
            @Parameter(description = "Username") @PathVariable String username) {
        UserDto user = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * Get all users with pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieves all users with pagination")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<UserDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    /**
     * Search users
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search users", description = "Search users by username, email, first name, or last name")
    public ResponseEntity<ApiResponse<List<UserDto>>> searchUsers(
            @Parameter(description = "Search term") @RequestParam String q,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "username") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<UserDto> users = userService.searchUsers(q, pageable);
        return ResponseEntity.ok(ApiResponse.success("Search results retrieved successfully", users));
    }

    /**
     * Get users by role
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get users by role", description = "Retrieves users with a specific role")
    public ResponseEntity<ApiResponse<List<UserDto>>> getUsersByRole(
            @Parameter(description = "User role") @PathVariable Role role,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());
        Page<UserDto> users = userService.getUsersByRole(role, pageable);
        return ResponseEntity.ok(ApiResponse.success("Users with role " + role + " retrieved successfully", users));
    }

    /**
     * Update user
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.getUserById(#id).username == authentication.name")
    @Operation(summary = "Update user", description = "Updates an existing user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @Valid @RequestBody UserDto userDto) {
        UserDto updatedUser = userService.updateUser(id, userDto);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updatedUser));
    }

    /**
     * Delete user
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Deletes a user from the system")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "User ID") @PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    /**
     * Update user status (enable/disable)
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user status", description = "Enable or disable a user")
    public ResponseEntity<ApiResponse<UserDto>> updateUserStatus(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @Parameter(description = "Enabled status") @RequestParam boolean enabled) {
        UserDto updatedUser = userService.updateUserStatus(id, enabled);
        String message = enabled ? "User enabled successfully" : "User disabled successfully";
        return ResponseEntity.ok(ApiResponse.success(message, updatedUser));
    }

    /**
     * Add role to user
     */
    @PostMapping("/{id}/roles/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add role to user", description = "Adds a role to a user")
    public ResponseEntity<ApiResponse<UserDto>> addRoleToUser(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @Parameter(description = "Role to add") @PathVariable Role role) {
        UserDto updatedUser = userService.addRoleToUser(id, role);
        return ResponseEntity.ok(ApiResponse.success("Role " + role + " added to user successfully", updatedUser));
    }

    /**
     * Remove role from user
     */
    @DeleteMapping("/{id}/roles/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove role from user", description = "Removes a role from a user")
    public ResponseEntity<ApiResponse<UserDto>> removeRoleFromUser(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @Parameter(description = "Role to remove") @PathVariable Role role) {
        UserDto updatedUser = userService.removeRoleFromUser(id, role);
        return ResponseEntity.ok(ApiResponse.success("Role " + role + " removed from user successfully", updatedUser));
    }

    /**
     * Get user statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user statistics", description = "Retrieves user statistics by role")
    public ResponseEntity<ApiResponse<Object>> getUserStats() {
        var stats = new Object() {
            public final long totalUsers = userService.getUserCountByRole(Role.USER);
            public final long totalAdmins = userService.getUserCountByRole(Role.ADMIN);
            public final long totalModerators = userService.getUserCountByRole(Role.MODERATOR);
        };
        return ResponseEntity.ok(ApiResponse.success("User statistics retrieved successfully", stats));
    }

    /**
     * Check if username exists
     */
    @GetMapping("/check/username/{username}")
    @Operation(summary = "Check username availability", description = "Checks if a username is available")
    public ResponseEntity<ApiResponse<Object>> checkUsernameAvailability(
            @Parameter(description = "Username to check") @PathVariable String username) {
        boolean exists = userService.existsByUsername(username);
        var result = new Object() {
            public final String username = username;
            public final boolean available = !exists;
            public final boolean exists = exists;
        };
        return ResponseEntity.ok(ApiResponse.success("Username availability checked", result));
    }

    /**
     * Check if email exists
     */
    @GetMapping("/check/email/{email}")
    @Operation(summary = "Check email availability", description = "Checks if an email is available")
    public ResponseEntity<ApiResponse<Object>> checkEmailAvailability(
            @Parameter(description = "Email to check") @PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        var result = new Object() {
            public final String email = email;
            public final boolean available = !exists;
            public final boolean exists = exists;
        };
        return ResponseEntity.ok(ApiResponse.success("Email availability checked", result));
    }
}
