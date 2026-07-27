package com.greenharvest.adjustment.service;

import com.greenharvest.adjustment.api.dto.CreateAdjustmentRequest;
import com.greenharvest.adjustment.api.dto.InventoryAdjustmentResponse;
import com.greenharvest.adjustment.model.AdjustmentType;
import com.greenharvest.adjustment.model.InventoryAdjustment;
import com.greenharvest.adjustment.repository.InventoryAdjustmentRepository;
import com.greenharvest.common.exception.ResourceNotFoundException;
import com.greenharvest.common.exception.ValidationException;
import com.greenharvest.product.model.Product;
import com.greenharvest.product.repository.ProductRepository;
import com.greenharvest.user.model.User;
import com.greenharvest.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryAdjustmentService {

    private final InventoryAdjustmentRepository adjustmentRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public InventoryAdjustmentResponse createAdjustment(CreateAdjustmentRequest request) {
        // 1. MANUAL VALIDATIONS (Requirement 7)
        if (request.productId() == null) {
            throw new ValidationException("Product ID is required");
        }
        if (request.type() == null) {
            throw new ValidationException("Adjustment type is required");
        }
        if (request.quantity() == null) {
            throw new ValidationException("Quantity is required");
        }
        if (request.reason() == null || request.reason().trim().isBlank()) {
            throw new ValidationException("Adjustment reason is required");
        }

        // 2. FETCH PRODUCT
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.productId()));

        // 3. DETERMINE QUANTITY DELTA & PREVENT NEGATIVE STOCK
        int currentStock = product.getCurrentStock() != null ? product.getCurrentStock() : 0;
        int delta = calculateStockDelta(request.type(), request.quantity());
        int newStock = currentStock + delta;

        if (newStock < 0) {
            throw new ValidationException(
                    "Insufficient stock for adjustment. Current stock: " + currentStock +
                            ", Requested adjustment delta: " + delta
            );
        }

        // 4. GET AUTHENTICATED USER
        User currentUser = getAuthenticatedUser();

        // 5. UPDATE PRODUCT STOCK
        product.setCurrentStock(newStock);
        productRepository.save(product);

        // 6. SAVE ADJUSTMENT RECORD
        InventoryAdjustment adjustment = InventoryAdjustment.builder()
                .product(product)
                .type(request.type())
                .quantity(delta) // Store actual signed delta applied to stock
                .reason(request.reason().trim())
                .adjustedBy(currentUser)
                .build();

        InventoryAdjustment savedAdjustment = adjustmentRepository.save(adjustment);

        return mapToResponse(savedAdjustment);
    }

    @Transactional(readOnly = true)
    public List<InventoryAdjustmentResponse> getAllAdjustments() {
        return adjustmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryAdjustmentResponse> getAdjustmentsByProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with ID: " + productId);
        }
        return adjustmentRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // --- HELPER METHODS ---

    private int calculateStockDelta(AdjustmentType type, int inputQuantity) {
        int absQty = Math.abs(inputQuantity);

        switch (type) {
            case RETURN:
                if (inputQuantity <= 0) {
                    throw new ValidationException("Return quantity must be greater than 0");
                }
                return absQty; // Increments stock

            case DAMAGED:
            case EXPIRED:
                if (inputQuantity <= 0) {
                    throw new ValidationException("Adjustment quantity for " + type + " must be greater than 0");
                }
                return -absQty; // Decrements stock

            case MANUAL_CORRECTION:
                if (inputQuantity == 0) {
                    throw new ValidationException("Manual correction quantity cannot be 0");
                }
                return inputQuantity; // Can be positive or negative

            default:
                throw new ValidationException("Unsupported adjustment type: " + type);
        }
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ValidationException("Authentication required to perform an inventory adjustment");
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user record not found"));
    }

    private InventoryAdjustmentResponse mapToResponse(InventoryAdjustment adj) {
        return new InventoryAdjustmentResponse(
                adj.getId(),
                adj.getProduct().getId(),
                adj.getProduct().getName(),
                adj.getProduct().getSku(),
                adj.getType(),
                adj.getQuantity(),
                adj.getReason(),
                adj.getAdjustedBy().getId(),
                adj.getAdjustedBy().getFullName(),
                adj.getCreatedAt()
        );
    }
}