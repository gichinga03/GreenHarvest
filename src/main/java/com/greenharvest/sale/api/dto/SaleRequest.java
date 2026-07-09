package com.greenharvest.sale.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record SaleRequest(
        String customerName,
        List<ItemRequest> items,
        String remarks
) {
    public record ItemRequest(
            Long productId,
            Integer quantity,
            BigDecimal unitPrice
    ) {}
}