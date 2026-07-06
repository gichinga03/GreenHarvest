package com.greenharvest.sale.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SaleResponse(
        Long id,
        String orderNumber,
        String customerName,
        BigDecimal totalAmount,
        String remarks,
        String recordedBy,
        List<ItemResponse> items,
        LocalDateTime createdAt
) {
    public record ItemResponse(
            Long id,
            Long productId,
            String productName,
            String sku,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice
    ) {}
}