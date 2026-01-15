package com.example.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.example.backend.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    // Database configuration is handled through application.properties
    // This class can be extended for custom database configurations
}