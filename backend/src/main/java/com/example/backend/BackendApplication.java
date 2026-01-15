package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Spring Boot Application Class
 * 
 * This class serves as the entry point for the Spring Boot application.
 * It enables JPA auditing for automatic timestamp management,
 * caching for performance optimization, and transaction management.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableTransactionManagement
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
