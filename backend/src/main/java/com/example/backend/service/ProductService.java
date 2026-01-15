package com.example.backend.service;

import com.example.backend.entity.Product;
import com.example.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable);
    }
    
    public Page<Product> getProductsByCategory(String category, Pageable pageable) {
        return productRepository.findByActiveTrueAndCategory(category, pageable);
    }
    
    public Page<Product> searchProducts(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return getAllProducts(pageable);
        }
        return productRepository.findActiveProductsBySearch(search.trim(), pageable);
    }
    
    public Page<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findActiveProductsByPriceRange(minPrice, maxPrice, pageable);
    }
    
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id)
                .filter(Product::getActive);
    }
    
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }
    
    public Optional<Product> updateProduct(Long id, Product productDetails) {
        return productRepository.findById(id)
                .filter(Product::getActive)
                .map(product -> {
                    product.setName(productDetails.getName());
                    product.setDescription(productDetails.getDescription());
                    product.setPrice(productDetails.getPrice());
                    product.setQuantity(productDetails.getQuantity());
                    product.setCategory(productDetails.getCategory());
                    return productRepository.save(product);
                });
    }
    
    public boolean deleteProduct(Long id) {
        return productRepository.findById(id)
                .filter(Product::getActive)
                .map(product -> {
                    product.setActive(false);
                    productRepository.save(product);
                    return true;
                })
                .orElse(false);
    }
    
    public List<String> getAllCategories() {
        return productRepository.findDistinctCategories();
    }
    
    public List<Product> getLowStockProducts(Integer threshold) {
        return productRepository.findByActiveTrueAndQuantityLessThan(threshold != null ? threshold : 10);
    }
    
    public Optional<Product> updateStock(Long id, Integer quantity) {
        return productRepository.findById(id)
                .filter(Product::getActive)
                .map(product -> {
                    product.setQuantity(quantity);
                    return productRepository.save(product);
                });
    }
}