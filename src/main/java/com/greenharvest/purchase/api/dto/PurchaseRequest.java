package com.greenharvest.purchase.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record PurchaseRequest(
        String invoiceNumber,
        Long supplierId,
        List<ItemRequest> items,
        String remarks
) {
        public record ItemRequest(
                Long productId,
                Integer quantity,
                BigDecimal unitPrice
        ) {}
}