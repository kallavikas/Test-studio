package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for monitoring application status
 */
@RestController
@RequestMapping("/health")
@Tag(name = "Health Check", description = "Application health monitoring endpoints")
public class HealthController implements HealthIndicator {

    @Autowired
    private DataSource dataSource;

    /**
     * Basic health check endpoint
     */
    @GetMapping
    @Operation(summary = "Health check", description = "Returns application health status")
    public ResponseEntity<ApiResponse<Object>> health() {
        Health health = health();
        
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("status", health.getStatus().getCode());
        healthData.put("timestamp", LocalDateTime.now());
        healthData.put("details", health.getDetails());
        
        return ResponseEntity.ok(ApiResponse.success("Application is healthy", healthData));
    }

    /**
     * Detailed health check with component status
     */
    @GetMapping("/detailed")
    @Operation(summary = "Detailed health check", description = "Returns detailed health status of all components")
    public ResponseEntity<ApiResponse<Object>> detailedHealth() {
        Map<String, Object> healthDetails = new HashMap<>();
        
        // Application status
        healthDetails.put("application", Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "uptime", getUptime()
        ));
        
        // Database status
        healthDetails.put("database", getDatabaseHealth());
        
        // Memory status
        healthDetails.put("memory", getMemoryHealth());
        
        // Disk space status
        healthDetails.put("diskSpace", getDiskSpaceHealth());
        
        return ResponseEntity.ok(ApiResponse.success("Detailed health check completed", healthDetails));
    }

    /**
     * Readiness probe for Kubernetes
     */
    @GetMapping("/ready")
    @Operation(summary = "Readiness probe", description = "Kubernetes readiness probe endpoint")
    public ResponseEntity<ApiResponse<Object>> readiness() {
        // Check if application is ready to serve requests
        boolean isReady = isDatabaseConnected() && isApplicationInitialized();
        
        Map<String, Object> readinessData = Map.of(
            "ready", isReady,
            "timestamp", LocalDateTime.now(),
            "checks", Map.of(
                "database", isDatabaseConnected(),
                "application", isApplicationInitialized()
            )
        );
        
        if (isReady) {
            return ResponseEntity.ok(ApiResponse.success("Application is ready", readinessData));
        } else {
            return ResponseEntity.status(503)
                    .body(ApiResponse.error("SERVICE_UNAVAILABLE", "Application is not ready"));
        }
    }

    /**
     * Liveness probe for Kubernetes
     */
    @GetMapping("/live")
    @Operation(summary = "Liveness probe", description = "Kubernetes liveness probe endpoint")
    public ResponseEntity<ApiResponse<Object>> liveness() {
        // Simple liveness check - if this endpoint responds, the app is alive
        Map<String, Object> livenessData = Map.of(
            "alive", true,
            "timestamp", LocalDateTime.now()
        );
        
        return ResponseEntity.ok(ApiResponse.success("Application is alive", livenessData));
    }

    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();
        
        try {
            // Check database connectivity
            if (isDatabaseConnected()) {
                builder.up()
                        .withDetail("database", "Connected")
                        .withDetail("timestamp", LocalDateTime.now());
            } else {
                builder.down()
                        .withDetail("database", "Disconnected");
            }
            
            // Add memory information
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            builder.withDetail("memory", Map.of(
                "max", formatBytes(maxMemory),
                "total", formatBytes(totalMemory),
                "used", formatBytes(usedMemory),
                "free", formatBytes(freeMemory)
            ));
            
        } catch (Exception e) {
            builder.down()
                    .withDetail("error", e.getMessage());
        }
        
        return builder.build();
    }

    private boolean isDatabaseConnected() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5 second timeout
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isApplicationInitialized() {
        // Add any application-specific initialization checks here
        return true;
    }

    private Map<String, Object> getDatabaseHealth() {
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(5);
            return Map.of(
                "status", isValid ? "UP" : "DOWN",
                "database", connection.getMetaData().getDatabaseProductName(),
                "version", connection.getMetaData().getDatabaseProductVersion(),
                "url", connection.getMetaData().getURL()
            );
        } catch (Exception e) {
            return Map.of(
                "status", "DOWN",
                "error", e.getMessage()
            );
        }
    }

    private Map<String, Object> getMemoryHealth() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        double usagePercentage = (double) usedMemory / maxMemory * 100;
        
        return Map.of(
            "status", usagePercentage > 90 ? "WARNING" : "UP",
            "max", formatBytes(maxMemory),
            "total", formatBytes(totalMemory),
            "used", formatBytes(usedMemory),
            "free", formatBytes(freeMemory),
            "usagePercentage", String.format("%.2f%%", usagePercentage)
        );
    }

    private Map<String, Object> getDiskSpaceHealth() {
        try {
            java.io.File root = new java.io.File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            
            double usagePercentage = (double) usedSpace / totalSpace * 100;
            
            return Map.of(
                "status", usagePercentage > 90 ? "WARNING" : "UP",
                "total", formatBytes(totalSpace),
                "used", formatBytes(usedSpace),
                "free", formatBytes(freeSpace),
                "usagePercentage", String.format("%.2f%%", usagePercentage)
            );
        } catch (Exception e) {
            return Map.of(
                "status", "UNKNOWN",
                "error", e.getMessage()
            );
        }
    }

    private String getUptime() {
        long uptimeMs = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%dd %dh %dm %ds", days, hours % 24, minutes % 60, seconds % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
