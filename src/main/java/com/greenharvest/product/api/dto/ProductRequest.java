package com.greenharvest.product.api.dto;

import java.math.BigDecimal;

public record ProductRequest(
        String name,
        String sku,
        BigDecimal price,
        Integer minimumStockLevel,
        String description
) {}