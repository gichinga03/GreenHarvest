package com.greenharvest.product.service;

import com.greenharvest.common.exception.DuplicateResourceException;
import com.greenharvest.common.exception.ResourceNotFoundException;
import com.greenharvest.common.exception.ValidationException;
import com.greenharvest.product.api.dto.ProductRequest;
import com.greenharvest.product.api.dto.ProductResponse;
import com.greenharvest.product.model.Product;
import com.greenharvest.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        //  MANUAL DATA VALIDATION (Requirement 7)
        validateProductRequest(request);

        if (productRepository.existsBySku(request.sku().toUpperCase().trim())) {
            throw new DuplicateResourceException("Product SKU already exists: " + request.sku());
        }

        Product product = Product.builder()
                .sku(request.sku().toUpperCase().trim())
                .name(request.name().trim())
                .description(request.description() != null ? request.description().trim() : null)
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

        // MANUAL DATA VALIDATION (Requirement 7)
        validateProductRequest(request);

        String formattedSku = request.sku().toUpperCase().trim();

        // If changing SKU, check for collisions
        if (!product.getSku().equalsIgnoreCase(formattedSku) && productRepository.existsBySku(formattedSku)) {
            throw new DuplicateResourceException("Product SKU already taken: " + request.sku());
        }

        product.setSku(formattedSku);
        product.setName(request.name().trim());
        product.setDescription(request.description() != null ? request.description().trim() : null) ;
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


    private void validateProductRequest(ProductRequest request) {
        if (request.name() == null || request.name().trim().isBlank()) {
            throw new ValidationException("Product name is required");
        }
        if (request.sku() == null || request.sku().trim().isBlank()) {
            throw new ValidationException("SKU is required");
        }
        // Enforce the strict GreenHarvest standard format pattern match: GH-XXX-999
        if (!request.sku().toUpperCase().trim().matches("^GH-[A-Z]{3}-\\d{3}$")) {
            throw new ValidationException("SKU must match the pattern 'GH-XXX-999' (e.g., GH-APP-001)");
        }
        if (request.price() == null || request.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Price must be a positive value greater than zero");
        }
        if (request.minimumStockLevel() == null || request.minimumStockLevel() < 0) {
            throw new ValidationException("Minimum stock level cannot be negative");
        }
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