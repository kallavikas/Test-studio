package com.example.backend.security;

import com.example.backend.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT Token Provider for generating and validating JWT tokens
 */
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpirationTime;
    private final long refreshTokenExpirationTime;
    
    // In-memory token blacklist (in production, use Redis or database)
    private final Set<String> blacklistedTokens = new HashSet<>();

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration}") long accessTokenExpirationTime,
            @Value("${app.jwt.refresh-expiration}") long refreshTokenExpirationTime) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpirationTime = accessTokenExpirationTime;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
    }

    /**
     * Generate access token for user
     * @param user the user
     * @return JWT access token
     */
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationTime);

        String roles = user.getRoles().stream()
                .map(role -> role.getAuthority())
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("userId", user.getId().toString())
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .claim("type", "access")
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Generate refresh token for user
     * @param user the user
     * @return JWT refresh token
     */
    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationTime);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("userId", user.getId().toString())
                .claim("type", "refresh")
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Get username from JWT token
     * @param token the JWT token
     * @return username
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * Get user ID from JWT token
     * @param token the JWT token
     * @return user ID
     */
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userId", String.class);
    }

    /**
     * Get roles from JWT token
     * @param token the JWT token
     * @return comma-separated roles
     */
    public String getRolesFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("roles", String.class);
    }

    /**
     * Get token type from JWT token
     * @param token the JWT token
     * @return token type (access or refresh)
     */
    public String getTokenType(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("type", String.class);
    }

    /**
     * Get token expiration date
     * @param token the JWT token
     * @return expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration();
    }

    /**
     * Validate JWT token
     * @param token the JWT token
     * @return true if token is valid
     */
    public boolean validateToken(String token) {
        try {
            // Check if token is blacklisted
            if (blacklistedTokens.contains(token)) {
                return false;
            }

            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Check if token is expired
     * @param token the JWT token
     * @return true if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Invalidate token by adding to blacklist
     * @param token the JWT token to invalidate
     */
    public void invalidateToken(String token) {
        blacklistedTokens.add(token);
        
        // Clean up expired tokens from blacklist to prevent memory leaks
        cleanupBlacklist();
    }

    /**
     * Check if token is blacklisted
     * @param token the JWT token
     * @return true if token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    /**
     * Get access token expiration time
     * @return expiration time in milliseconds
     */
    public long getAccessTokenExpirationTime() {
        return accessTokenExpirationTime;
    }

    /**
     * Get refresh token expiration time
     * @return expiration time in milliseconds
     */
    public long getRefreshTokenExpirationTime() {
        return refreshTokenExpirationTime;
    }

    /**
     * Clean up expired tokens from blacklist
     */
    private void cleanupBlacklist() {
        // Remove expired tokens from blacklist
        blacklistedTokens.removeIf(token -> {
            try {
                return isTokenExpired(token);
            } catch (Exception e) {
                // If we can't parse the token, remove it
                return true;
            }
        });
    }

    /**
     * Extract token from Authorization header
     * @param authHeader the Authorization header
     * @return JWT token without "Bearer " prefix
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Get all claims from token
     * @param token the JWT token
     * @return claims
     */
    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
