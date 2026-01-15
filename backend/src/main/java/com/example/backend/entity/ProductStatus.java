package com.example.backend.entity;

/**
 * Enumeration representing the status of a product
 */
public enum ProductStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    DISCONTINUED("Discontinued"),
    OUT_OF_STOCK("Out of Stock");

    private final String displayName;

    ProductStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
