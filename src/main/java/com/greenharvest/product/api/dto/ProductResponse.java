package com.greenharvest.product.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        Integer currentStock,
        Integer minimumStockLevel,
        boolean isLowStock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}