package com.greenharvest.sale.service;

import com.greenharvest.common.exception.DuplicateResourceException;
import com.greenharvest.common.exception.InsufficientStockException;
import com.greenharvest.common.exception.ResourceNotFoundException;
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
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // 🎯 AUTOMATIC SEQUENCE GENERATION
        // Formats out to e.g., GH-SAL-20260706-A8F2B
        String generatedOrderNumber = String.format("GH-SAL-%s-%s",
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd").format(java.time.LocalDateTime.now()),
                java.util.UUID.randomUUID().toString().substring(0, 5).toUpperCase()
        );

        Sale sale = Sale.builder()
                .orderNumber(generatedOrderNumber) // Assigned cleanly here
                .customerName(request.customerName().trim())
                .remarks(request.remarks())
                .recordedBy(currentUserEmail)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal grandTotal = BigDecimal.ZERO;

        for (SaleRequest.ItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + itemReq.productId()));

            if (product.getCurrentStock() < itemReq.quantity()) {
                throw new InsufficientStockException(
                        String.format("Insufficient stock for product '%s' (SKU: %s). Requested: %d, Available: %d",
                                product.getName(), product.getSku(), itemReq.quantity(), product.getCurrentStock())
                );
            }

            // 🎯 AUTOMATIC CATALOG PRICE FALLBACK CALCULATION
            // If itemReq.unitPrice() is null, default directly to product.getPrice()
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