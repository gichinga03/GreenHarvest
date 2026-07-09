package com.greenharvest.sale.service;

import com.greenharvest.common.exception.InsufficientStockException;
import com.greenharvest.common.exception.ResourceNotFoundException;
import com.greenharvest.common.exception.ValidationException;
import com.greenharvest.product.model.Product;
import com.greenharvest.product.repository.ProductRepository;
import com.greenharvest.sale.api.dto.SaleRequest;
import com.greenharvest.sale.api.dto.SaleResponse;
import com.greenharvest.sale.model.Sale;
import com.greenharvest.sale.model.SaleItem;
import com.greenharvest.sale.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;

    @Transactional
    public SaleResponse recordSale(SaleRequest request) {
        //  1. MANUAL PARENT ORDER VALIDATION (Requirement 7)
        if (request.customerName() == null || request.customerName().trim().isBlank()) {
            throw new ValidationException("Customer supermarket name is required");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new ValidationException("Sale dispatch must contain at least one item");
        }

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        //  AUTOMATIC SEQUENCE GENERATION
        String generatedOrderNumber = String.format("GH-SAL-%s-%s",
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd").format(java.time.LocalDateTime.now()),
                java.util.UUID.randomUUID().toString().substring(0, 5).toUpperCase()
        );

        Sale sale = Sale.builder()
                .orderNumber(generatedOrderNumber)
                .customerName(request.customerName().trim())
                .remarks(request.remarks() != null ? request.remarks().trim() : null)
                .recordedBy(currentUserEmail)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal grandTotal = BigDecimal.ZERO;

        //  2. PROCESS AND MANUALLY VALIDATE DISPATCH ITEMS
        for (SaleRequest.ItemRequest itemReq : request.items()) {
            if (itemReq.productId() == null) {
                throw new ValidationException("Product ID is required for all item records");
            }
            if (itemReq.quantity() == null || itemReq.quantity() < 1) {
                throw new ValidationException("Quantity must be at least 1 unit");
            }
            if (itemReq.unitPrice() != null && itemReq.unitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Unit price must be positive if specified");
            }

            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + itemReq.productId()));

            //  CHALLENGE B: Enforce physical warehouse limits before altering numbers
            if (product.getCurrentStock() < itemReq.quantity()) {
                throw new InsufficientStockException(
                        String.format("Insufficient stock for product '%s' (SKU: %s). Requested: %d, Available: %d",
                                product.getName(), product.getSku(), itemReq.quantity(), product.getCurrentStock())
                );
            }

            // AUTOMATIC CATALOG PRICE FALLBACK CALCULATION
            BigDecimal finalUnitPrice = (itemReq.unitPrice() != null) ? itemReq.unitPrice() : product.getPrice();

            // Calculate derived line item totals using the evaluated final unit price
            BigDecimal lineTotal = finalUnitPrice.multiply(BigDecimal.valueOf(itemReq.quantity()));
            grandTotal = grandTotal.add(lineTotal);

            SaleItem lineItem = SaleItem.builder()
                    .product(product)
                    .quantity(itemReq.quantity())
                    .unitPrice(finalUnitPrice)
                    .totalPrice(lineTotal)
                    .build();

            sale.addItem(lineItem);

            // Decrement stock levels securely
            product.setCurrentStock(product.getCurrentStock() - itemReq.quantity());
            productRepository.save(product);
        }

        sale.setTotalAmount(grandTotal);
        Sale savedSale = saleRepository.save(sale);

        return mapToResponse(savedSale);
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> getAllSales() {
        return saleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SaleResponse getSaleById(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale record entry not found with ID: " + id));
        return mapToResponse(sale);
    }

    private SaleResponse mapToResponse(Sale sale) {
        List<SaleResponse.ItemResponse> itemResponses = sale.getItems().stream()
                .map(item -> new SaleResponse.ItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getSku(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()
                )).collect(Collectors.toList());

        return new SaleResponse(
                sale.getId(),
                sale.getOrderNumber(),
                sale.getCustomerName(),
                sale.getTotalAmount(),
                sale.getRemarks(),
                sale.getRecordedBy(),
                itemResponses,
                sale.getCreatedAt()
        );
    }
}