package com.ecommerce.config;

import com.ecommerce.Repository.OrderRepository;
import com.ecommerce.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;  // Dependency to check product references

    @Autowired
    public ProductService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void deleteProductById(int productId) {
        if (OrderRepository.existsByProductId(productId)) {
            throw new IllegalStateException("Product cannot be deleted because it is part of one or more orders.");
        }
        try {
            productRepository.deleteById(productId);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting product with ID: " + productId, e);
        }
    }
}