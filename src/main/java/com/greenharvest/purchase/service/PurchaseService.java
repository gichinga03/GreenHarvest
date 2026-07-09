package com.greenharvest.purchase.service;

import com.greenharvest.common.exception.DuplicateResourceException;
import com.greenharvest.common.exception.ResourceNotFoundException;
import com.greenharvest.common.exception.ValidationException;
import com.greenharvest.product.model.Product;
import com.greenharvest.product.repository.ProductRepository;
import com.greenharvest.purchase.api.dto.PurchaseRequest;
import com.greenharvest.purchase.api.dto.PurchaseResponse;
import com.greenharvest.purchase.model.Purchase;
import com.greenharvest.purchase.model.PurchaseItem;
import com.greenharvest.purchase.repository.PurchaseRepository;
import com.greenharvest.supplier.model.Supplier;
import com.greenharvest.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    @Transactional
    public PurchaseResponse recordPurchase(PurchaseRequest request) {
        // 🎯 1. MANUAL PARENT REQUEST VALIDATION (Requirement 7)
        if (request.supplierId() == null) {
            throw new ValidationException("Supplier ID is required");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new ValidationException("Purchase order must contain at least one product line item");
        }

        String finalInvoiceNumber;

        //  AUTOMATIC FALLBACK REFERENCE CALCULATION
        if (request.invoiceNumber() == null || request.invoiceNumber().trim().isBlank()) {
            finalInvoiceNumber = String.format("GH-PUR-%s-%s",
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd").format(java.time.LocalDateTime.now()),
                    java.util.UUID.randomUUID().toString().substring(0, 5).toUpperCase()
            );
        } else {
            finalInvoiceNumber = request.invoiceNumber().toUpperCase().trim();
            // Prevent duplicate manual invoices
            if (purchaseRepository.existsByInvoiceNumber(finalInvoiceNumber)) {
                throw new DuplicateResourceException("Invoice number already processed: " + finalInvoiceNumber);
            }
        }

        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + request.supplierId()));

        if (!supplier.isActive()) {
            throw new ValidationException("Cannot accept goods from an inactive/suspended supplier.");
        }

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        Purchase purchase = Purchase.builder()
                .invoiceNumber(finalInvoiceNumber)
                .supplier(supplier)
                .remarks(request.remarks() != null ? request.remarks().trim() : null)
                .recordedBy(currentUserEmail)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal grandTotal = BigDecimal.ZERO;

        // 2. PROCESS AND MANUALLY VALIDATE LINE ITEMS
        for (PurchaseRequest.ItemRequest itemReq : request.items()) {
            if (itemReq.productId() == null) {
                throw new ValidationException("Product ID is required for all line items");
            }
            if (itemReq.quantity() == null || itemReq.quantity() < 1) {
                throw new ValidationException("Quantity must be at least 1 unit");
            }
            if (itemReq.unitPrice() == null || itemReq.unitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Unit cost price must be positive");
            }

            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + itemReq.productId()));

            // SERVICE CALCULATIONS: Line item total = quantity * unitPrice
            BigDecimal lineTotal = itemReq.unitPrice().multiply(BigDecimal.valueOf(itemReq.quantity()));
            grandTotal = grandTotal.add(lineTotal);

            PurchaseItem lineItem = PurchaseItem.builder()
                    .product(product)
                    .quantity(itemReq.quantity())
                    .unitPrice(itemReq.unitPrice())
                    .totalPrice(lineTotal)
                    .build();

            purchase.addItem(lineItem);

            // INVENTORY CALCULATION UPDATE: Increment physical stock counts
            product.setCurrentStock(product.getCurrentStock() + itemReq.quantity());
            productRepository.save(product);
        }

        purchase.setTotalAmount(grandTotal);
        Purchase savedPurchase = purchaseRepository.save(purchase);

        return mapToResponse(savedPurchase);
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponse> getAllPurchases() {
        return purchaseRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PurchaseResponse getPurchaseById(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase ledger entry not found with ID: " + id));
        return mapToResponse(purchase);
    }

    private PurchaseResponse mapToResponse(Purchase purchase) {
        List<PurchaseResponse.ItemResponse> itemResponses = purchase.getItems().stream()
                .map(item -> new PurchaseResponse.ItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getSku(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()
                )).collect(Collectors.toList());

        return new PurchaseResponse(
                purchase.getId(),
                purchase.getInvoiceNumber(),
                purchase.getSupplier().getId(),
                purchase.getSupplier().getName(),
                purchase.getTotalAmount(),
                purchase.getRemarks(),
                purchase.getRecordedBy(),
                itemResponses,
                purchase.getCreatedAt()
        );
    }
}