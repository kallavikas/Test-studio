package com.example.backend.service;

import com.example.backend.dto.ProductDto;
import com.example.backend.entity.Product;
import com.example.backend.entity.ProductStatus;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.DuplicateResourceException;
import com.example.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service class for Product entity operations
 */
@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Create a new product
     * @param productDto the product data
     * @return created product DTO
     */
    @CacheEvict(value = "products", allEntries = true)
    public ProductDto createProduct(ProductDto productDto) {
        // Check if SKU already exists
        if (productDto.getSku() != null && productRepository.existsBySku(productDto.getSku())) {
            throw new DuplicateResourceException("Product with SKU already exists: " + productDto.getSku());
        }

        Product product = convertToEntity(productDto);
        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct);
    }

    /**
     * Get product by ID
     * @param id the product ID
     * @return product DTO
     */
    @Cacheable(value = "products", key = "#id")
    @Transactional(readOnly = true)
    public ProductDto getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return convertToDto(product);
    }

    /**
     * Get product by SKU
     * @param sku the product SKU
     * @return product DTO
     */
    @Cacheable(value = "products", key = "#sku")
    @Transactional(readOnly = true)
    public ProductDto getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
        return convertToDto(product);
    }

    /**
     * Get all products with pagination
     * @param pageable pagination information
     * @return page of product DTOs
     */
    @Transactional(readOnly = true)
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(this::convertToDto);
    }

    /**
     * Search products by name or description
     * @param searchTerm the search term
     * @param pageable pagination information
     * @return page of product DTOs
     */
    @Transactional(readOnly = true)
    public Page<ProductDto> searchProducts(String searchTerm, Pageable pageable) {
        Page<Product> products = productRepository.searchProducts(searchTerm, pageable);
        return products.map(this::convertToDto);
    }

    /**
     * Get products by category
     * @param category the category
     * @param pageable pagination information
     * @return page of product DTOs
     */
    @Transactional(readOnly = true)
    public Page<ProductDto> getProductsByCategory(String category, Pageable pageable) {
        Page<Product> products = productRepository.findByCategory(category, pageable);
        return products.map(this::convertToDto);
    }

    /**
     * Get products by status
     * @param status the status
     * @param pageable pagination information
     * @return page of product DTOs
     */
    @Transactional(readOnly = true)
    public Page<ProductDto> getProductsByStatus(ProductStatus status, Pageable pageable) {
        Page<Product> products = productRepository.findByStatus(status, pageable);
        return products.map(this::convertToDto);
    }

    /**
     * Get products by brand
     * @param brand the brand
     * @param pageable pagination information
     * @return page of product DTOs
     */
    @Transactional(readOnly = true)
    public Page<ProductDto> getProductsByBrand(String brand, Pageable pageable) {
        Page<Product> products = productRepository.findByBrand(brand, pageable);
        return products.map(this::convertToDto);
    }

    /**
     * Get products within price range
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @param pageable pagination information
     * @return page of product DTOs
     */
    @Transactional(readOnly = true)
    public Page<ProductDto> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Page<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice, pageable);
        return products.map(this::convertToDto);
    }

    /**
     * Get available products (active and in stock)
     * @param pageable pagination information
     * @return page of product DTOs
     */
    @Transactional(readOnly = true)
    public Page<ProductDto> getAvailableProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAvailableProducts(pageable);
        return products.map(this::convertToDto);
    }

    /**
     * Get low stock products
     * @param threshold the stock threshold
     * @param pageable pagination information
     * @return page of product DTOs
     */
    @Transactional(readOnly = true)
    public Page<ProductDto> getLowStockProducts(Integer threshold, Pageable pageable) {
        Page<Product> products = productRepository.findLowStockProducts(threshold, pageable);
        return products.map(this::convertToDto);
    }

    /**
     * Get featured products
     * @param pageable pagination information
     * @return page of product DTOs
     */
    @Transactional(readOnly = true)
    public Page<ProductDto> getFeaturedProducts(Pageable pageable) {
        Page<Product> products = productRepository.findFeaturedProducts(pageable);
        return products.map(this::convertToDto);
    }

    /**
     * Advanced product search with multiple filters
     * @param name product name filter
     * @param category category filter
     * @param brand brand filter
     * @param minPrice minimum price filter
     * @param maxPrice maximum price filter
     * @param status status filter
     * @param pageable pagination information
     * @return page of product DTOs
     */
    @Transactional(readOnly = true)
    public Page<ProductDto> searchProductsWithFilters(
            String name, String category, String brand,
            BigDecimal minPrice, BigDecimal maxPrice,
            ProductStatus status, Pageable pageable) {
        Page<Product> products = productRepository.findProductsWithFilters(
                name, category, brand, minPrice, maxPrice, status, pageable);
        return products.map(this::convertToDto);
    }

    /**
     * Update product
     * @param id the product ID
     * @param productDto the updated product data
     * @return updated product DTO
     */
    @CacheEvict(value = "products", key = "#id")
    public ProductDto updateProduct(UUID id, ProductDto productDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Check if SKU is being changed and if it already exists
        if (productDto.getSku() != null &&
            !productDto.getSku().equals(existingProduct.getSku()) &&
            productRepository.existsBySku(productDto.getSku())) {
            throw new DuplicateResourceException("Product with SKU already exists: " + productDto.getSku());
        }

        // Update fields
        existingProduct.setName(productDto.getName());
        existingProduct.setDescription(productDto.getDescription());
        existingProduct.setPrice(productDto.getPrice());
        existingProduct.setCategory(productDto.getCategory());
        existingProduct.setStockQuantity(productDto.getStockQuantity());
        existingProduct.setSku(productDto.getSku());
        existingProduct.setImageUrl(productDto.getImageUrl());
        existingProduct.setWeight(productDto.getWeight());
        existingProduct.setBrand(productDto.getBrand());
        
        if (productDto.getStatus() != null) {
            existingProduct.setStatus(productDto.getStatus());
        }

        Product updatedProduct = productRepository.save(existingProduct);
        return convertToDto(updatedProduct);
    }

    /**
     * Update product status
     * @param id the product ID
     * @param status the new status
     * @return updated product DTO
     */
    @CacheEvict(value = "products", key = "#id")
    public ProductDto updateProductStatus(UUID id, ProductStatus status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        product.setStatus(status);
        Product updatedProduct = productRepository.save(product);
        return convertToDto(updatedProduct);
    }

    /**
     * Update product stock
     * @param id the product ID
     * @param stockQuantity the new stock quantity
     * @return updated product DTO
     */
    @CacheEvict(value = "products", key = "#id")
    public ProductDto updateProductStock(UUID id, Integer stockQuantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        product.setStockQuantity(stockQuantity);
        Product updatedProduct = productRepository.save(product);
        return convertToDto(updatedProduct);
    }

    /**
     * Delete product
     * @param id the product ID
     */
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    /**
     * Get distinct categories
     * @return list of categories
     */
    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return productRepository.findDistinctCategories();
    }

    /**
     * Get distinct brands
     * @return list of brands
     */
    @Transactional(readOnly = true)
    public List<String> getBrands() {
        return productRepository.findDistinctBrands();
    }

    /**
     * Get product count by status
     * @param status the status
     * @return count of products with the status
     */
    @Transactional(readOnly = true)
    public long getProductCountByStatus(ProductStatus status) {
        return productRepository.countByStatus(status);
    }

    /**
     * Get product count by category
     * @param category the category
     * @return count of products in the category
     */
    @Transactional(readOnly = true)
    public long getProductCountByCategory(String category) {
        return productRepository.countByCategory(category);
    }

    /**
     * Check if SKU exists
     * @param sku the SKU
     * @return true if exists
     */
    @Transactional(readOnly = true)
    public boolean existsBySku(String sku) {
        return productRepository.existsBySku(sku);
    }

    // Helper methods for entity-DTO conversion
    private ProductDto convertToDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getStockQuantity(),
                product.getSku(),
                product.getStatus(),
                product.getImageUrl(),
                product.getWeight(),
                product.getBrand(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private Product convertToEntity(ProductDto productDto) {
        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setCategory(productDto.getCategory());
        product.setStockQuantity(productDto.getStockQuantity() != null ? productDto.getStockQuantity() : 0);
        product.setSku(productDto.getSku());
        product.setStatus(productDto.getStatus() != null ? productDto.getStatus() : ProductStatus.ACTIVE);
        product.setImageUrl(productDto.getImageUrl());
        product.setWeight(productDto.getWeight());
        product.setBrand(productDto.getBrand());
        return product;
    }
}
