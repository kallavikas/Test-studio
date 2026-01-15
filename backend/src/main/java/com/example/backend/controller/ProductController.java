package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.ProductDto;
import com.example.backend.entity.ProductStatus;
import com.example.backend.service.ProductService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Product management operations
 */
@RestController
@RequestMapping("/products")
@Tag(name = "Product Management", description = "APIs for managing products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Create a new product
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new product", description = "Creates a new product in the system")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Product created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Product SKU already exists")
    })
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(
            @Valid @RequestBody ProductDto productDto) {
        ProductDto createdProduct = productService.createProduct(productDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", createdProduct));
    }

    /**
     * Get product by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves a product by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ApiResponse<ProductDto>> getProductById(
            @Parameter(description = "Product ID") @PathVariable UUID id) {
        ProductDto product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    /**
     * Get product by SKU
     */
    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Retrieves a product by its SKU")
    public ResponseEntity<ApiResponse<ProductDto>> getProductBySku(
            @Parameter(description = "Product SKU") @PathVariable String sku) {
        ProductDto product = productService.getProductBySku(sku);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    /**
     * Get all products with pagination
     */
    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieves all products with pagination and filtering")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getAllProducts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Filter by category") @RequestParam(required = false) String category,
            @Parameter(description = "Filter by brand") @RequestParam(required = false) String brand,
            @Parameter(description = "Filter by status") @RequestParam(required = false) ProductStatus status,
            @Parameter(description = "Minimum price") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Search term") @RequestParam(required = false) String search) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductDto> products;
        
        if (search != null && !search.trim().isEmpty()) {
            products = productService.searchProducts(search, pageable);
        } else if (category != null || brand != null || status != null || minPrice != null || maxPrice != null) {
            products = productService.searchProductsWithFilters(
                    null, category, brand, minPrice, maxPrice, status, pageable);
        } else {
            products = productService.getAllProducts(pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }

    /**
     * Search products
     */
    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products by name or description")
    public ResponseEntity<ApiResponse<List<ProductDto>>> searchProducts(
            @Parameter(description = "Search term") @RequestParam String q,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductDto> products = productService.searchProducts(q, pageable);
        return ResponseEntity.ok(ApiResponse.success("Search results retrieved successfully", products));
    }

    /**
     * Get products by category
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Get products by category", description = "Retrieves products in a specific category")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getProductsByCategory(
            @Parameter(description = "Product category") @PathVariable String category,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ProductDto> products = productService.getProductsByCategory(category, pageable);
        return ResponseEntity.ok(ApiResponse.success("Products in category " + category + " retrieved successfully", products));
    }

    /**
     * Get products by brand
     */
    @GetMapping("/brand/{brand}")
    @Operation(summary = "Get products by brand", description = "Retrieves products from a specific brand")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getProductsByBrand(
            @Parameter(description = "Product brand") @PathVariable String brand,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ProductDto> products = productService.getProductsByBrand(brand, pageable);
        return ResponseEntity.ok(ApiResponse.success("Products from brand " + brand + " retrieved successfully", products));
    }

    /**
     * Get available products
     */
    @GetMapping("/available")
    @Operation(summary = "Get available products", description = "Retrieves products that are active and in stock")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getAvailableProducts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ProductDto> products = productService.getAvailableProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success("Available products retrieved successfully", products));
    }

    /**
     * Get featured products
     */
    @GetMapping("/featured")
    @Operation(summary = "Get featured products", description = "Retrieves featured products")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getFeaturedProducts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "12") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDto> products = productService.getFeaturedProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success("Featured products retrieved successfully", products));
    }

    /**
     * Get low stock products
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get low stock products", description = "Retrieves products with low stock")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getLowStockProducts(
            @Parameter(description = "Stock threshold") @RequestParam(defaultValue = "10") Integer threshold,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("stockQuantity").ascending());
        Page<ProductDto> products = productService.getLowStockProducts(threshold, pageable);
        return ResponseEntity.ok(ApiResponse.success("Low stock products retrieved successfully", products));
    }

    /**
     * Update product
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update product", description = "Updates an existing product")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Product SKU already exists")
    })
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(
            @Parameter(description = "Product ID") @PathVariable UUID id,
            @Valid @RequestBody ProductDto productDto) {
        ProductDto updatedProduct = productService.updateProduct(id, productDto);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updatedProduct));
    }

    /**
     * Update product status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update product status", description = "Updates the status of a product")
    public ResponseEntity<ApiResponse<ProductDto>> updateProductStatus(
            @Parameter(description = "Product ID") @PathVariable UUID id,
            @Parameter(description = "Product status") @RequestParam ProductStatus status) {
        ProductDto updatedProduct = productService.updateProductStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Product status updated successfully", updatedProduct));
    }

    /**
     * Update product stock
     */
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update product stock", description = "Updates the stock quantity of a product")
    public ResponseEntity<ApiResponse<ProductDto>> updateProductStock(
            @Parameter(description = "Product ID") @PathVariable UUID id,
            @Parameter(description = "Stock quantity") @RequestParam Integer stockQuantity) {
        ProductDto updatedProduct = productService.updateProductStock(id, stockQuantity);
        return ResponseEntity.ok(ApiResponse.success("Product stock updated successfully", updatedProduct));
    }

    /**
     * Delete product
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete product", description = "Deletes a product from the system")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully"));
    }

    /**
     * Get product categories
     */
    @GetMapping("/categories")
    @Operation(summary = "Get product categories", description = "Retrieves all distinct product categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        List<String> categories = productService.getCategories();
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categories));
    }

    /**
     * Get product brands
     */
    @GetMapping("/brands")
    @Operation(summary = "Get product brands", description = "Retrieves all distinct product brands")
    public ResponseEntity<ApiResponse<List<String>>> getBrands() {
        List<String> brands = productService.getBrands();
        return ResponseEntity.ok(ApiResponse.success("Brands retrieved successfully", brands));
    }

    /**
     * Get product statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get product statistics", description = "Retrieves product statistics")
    public ResponseEntity<ApiResponse<Object>> getProductStats() {
        var stats = new Object() {
            public final long totalProducts = productService.getProductCountByStatus(ProductStatus.ACTIVE);
            public final long activeProducts = productService.getProductCountByStatus(ProductStatus.ACTIVE);
            public final long inactiveProducts = productService.getProductCountByStatus(ProductStatus.INACTIVE);
            public final long discontinuedProducts = productService.getProductCountByStatus(ProductStatus.DISCONTINUED);
            public final long outOfStockProducts = productService.getProductCountByStatus(ProductStatus.OUT_OF_STOCK);
        };
        return ResponseEntity.ok(ApiResponse.success("Product statistics retrieved successfully", stats));
    }

    /**
     * Check if SKU exists
     */
    @GetMapping("/check/sku/{sku}")
    @Operation(summary = "Check SKU availability", description = "Checks if a SKU is available")
    public ResponseEntity<ApiResponse<Object>> checkSkuAvailability(
            @Parameter(description = "SKU to check") @PathVariable String sku) {
        boolean exists = productService.existsBySku(sku);
        var result = new Object() {
            public final String sku = sku;
            public final boolean available = !exists;
            public final boolean exists = exists;
        };
        return ResponseEntity.ok(ApiResponse.success("SKU availability checked", result));
    }
}
