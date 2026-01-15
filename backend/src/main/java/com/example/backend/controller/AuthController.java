package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.AuthDto;
import com.example.backend.dto.UserDto;
import com.example.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Authentication operations
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "APIs for user authentication and registration")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * User registration
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    public ResponseEntity<ApiResponse<UserDto>> register(
            @Valid @RequestBody AuthDto.RegisterRequest registerRequest) {
        UserDto user = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", user));
    }

    /**
     * User login
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and returns JWT token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<ApiResponse<AuthDto.JwtResponse>> login(
            @Valid @RequestBody AuthDto.LoginRequest loginRequest) {
        AuthDto.JwtResponse jwtResponse = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success("Login successful", jwtResponse));
    }

    /**
     * Refresh JWT token
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT token", description = "Refreshes an expired JWT token using refresh token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    public ResponseEntity<ApiResponse<AuthDto.JwtResponse>> refreshToken(
            @Valid @RequestBody AuthDto.RefreshTokenRequest refreshTokenRequest) {
        AuthDto.JwtResponse jwtResponse = authService.refreshToken(refreshTokenRequest.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", jwtResponse));
    }

    /**
     * Logout user
     */
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Invalidates the current JWT token")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String token) {
        // Extract token from "Bearer " prefix
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }

    /**
     * Change password
     */
    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Changes the current user's password")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid current password")
    })
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody AuthDto.ChangePasswordRequest changePasswordRequest,
            @RequestHeader("Authorization") String token) {
        // Extract token from "Bearer " prefix
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        authService.changePassword(token, changePasswordRequest);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    /**
     * Validate JWT token
     */
    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token", description = "Validates the current JWT token")
    public ResponseEntity<ApiResponse<Object>> validateToken(
            @RequestHeader("Authorization") String token) {
        // Extract token from "Bearer " prefix
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        boolean isValid = authService.validateToken(token);
        var result = new Object() {
            public final boolean valid = isValid;
            public final String status = isValid ? "Token is valid" : "Token is invalid";
        };
        
        return ResponseEntity.ok(ApiResponse.success("Token validation completed", result));
    }
}
