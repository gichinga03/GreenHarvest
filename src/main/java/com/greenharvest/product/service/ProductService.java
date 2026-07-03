package com.greenharvest.product.service;

import com.greenharvest.common.exception.DuplicateResourceException;
import com.greenharvest.common.exception.ResourceNotFoundException;
import com.greenharvest.product.api.dto.ProductRequest;
import com.greenharvest.product.api.dto.ProductResponse;
import com.greenharvest.product.model.Product;
import com.greenharvest.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new DuplicateResourceException("Product SKU already exists: " + request.sku());
        }

        Product product = Product.builder()
                .sku(request.sku().toUpperCase().trim())
                .name(request.name().trim())
                .description(request.description())
                .price(request.price())
                .minimumStockLevel(request.minimumStockLevel())
                .currentStock(0) // New products explicitly start with 0 stock until a Purchase happens
                .build();

        return mapToResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // If changing SKU, check for collisions
        if (!product.getSku().equalsIgnoreCase(request.sku()) && productRepository.existsBySku(request.sku())) {
            throw new DuplicateResourceException("Product SKU already taken: " + request.sku());
        }

        product.setSku(request.sku().toUpperCase().trim());
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setMinimumStockLevel(request.minimumStockLevel());

        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    private ProductResponse mapToResponse(Product product) {
        boolean isLowStock = product.getCurrentStock() <= product.getMinimumStockLevel();
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrentStock(),
                product.getMinimumStockLevel(),
                isLowStock,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}