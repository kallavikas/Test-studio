package com.example.backend.repository;

import com.example.backend.entity.Product;
import com.example.backend.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Product entity operations
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    /**
     * Find product by SKU
     * @param sku the SKU to search for
     * @return Optional containing the product if found
     */
    Optional<Product> findBySku(String sku);

    /**
     * Find products by category
     * @param category the category to search for
     * @param pageable pagination information
     * @return Page of products in the specified category
     */
    Page<Product> findByCategory(String category, Pageable pageable);

    /**
     * Find products by status
     * @param status the status to search for
     * @param pageable pagination information
     * @return Page of products with the specified status
     */
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    /**
     * Find products by brand
     * @param brand the brand to search for
     * @param pageable pagination information
     * @return Page of products from the specified brand
     */
    Page<Product> findByBrand(String brand, Pageable pageable);

    /**
     * Search products by name or description
     * @param searchTerm the term to search for
     * @param pageable pagination information
     * @return Page of products matching the search term
     */
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find products within price range
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @param pageable pagination information
     * @return Page of products within the price range
     */
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Find products with stock quantity greater than specified amount
     * @param stockQuantity minimum stock quantity
     * @param pageable pagination information
     * @return Page of products with sufficient stock
     */
    Page<Product> findByStockQuantityGreaterThan(Integer stockQuantity, Pageable pageable);

    /**
     * Find products that are in stock and active
     * @param pageable pagination information
     * @return Page of available products
     */
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.stockQuantity > 0")
    Page<Product> findAvailableProducts(Pageable pageable);

    /**
     * Find products by category and status
     * @param category the category
     * @param status the status
     * @param pageable pagination information
     * @return Page of products matching category and status
     */
    Page<Product> findByCategoryAndStatus(String category, ProductStatus status, Pageable pageable);

    /**
     * Find low stock products (stock quantity less than specified threshold)
     * @param threshold the stock threshold
     * @param pageable pagination information
     * @return Page of low stock products
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity < :threshold AND p.status = 'ACTIVE'")
    Page<Product> findLowStockProducts(@Param("threshold") Integer threshold, Pageable pageable);

    /**
     * Get distinct categories
     * @return List of distinct categories
     */
    @Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category")
    List<String> findDistinctCategories();

    /**
     * Get distinct brands
     * @return List of distinct brands
     */
    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.brand IS NOT NULL ORDER BY p.brand")
    List<String> findDistinctBrands();

    /**
     * Count products by status
     * @param status the status to count
     * @return number of products with the specified status
     */
    long countByStatus(ProductStatus status);

    /**
     * Count products by category
     * @param category the category to count
     * @return number of products in the specified category
     */
    long countByCategory(String category);

    /**
     * Check if SKU exists
     * @param sku the SKU to check
     * @return true if SKU exists
     */
    boolean existsBySku(String sku);

    /**
     * Find featured products (top selling or promoted)
     * @param pageable pagination information
     * @return Page of featured products
     */
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.stockQuantity > 0 ORDER BY p.createdAt DESC")
    Page<Product> findFeaturedProducts(Pageable pageable);

    /**
     * Complex search with multiple filters
     * @param name product name filter
     * @param category category filter
     * @param brand brand filter
     * @param minPrice minimum price filter
     * @param maxPrice maximum price filter
     * @param status status filter
     * @param pageable pagination information
     * @return Page of products matching the filters
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:brand IS NULL OR p.brand = :brand) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:status IS NULL OR p.status = :status)")
    Page<Product> findProductsWithFilters(
        @Param("name") String name,
        @Param("category") String category,
        @Param("brand") String brand,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("status") ProductStatus status,
        Pageable pageable
    );
}
