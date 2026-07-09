package com.greenharvest.inventory.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record InventoryDashboardResponse(
        long totalProductsCount,
        long totalStockQuantity,
        BigDecimal totalInventoryValue,
        long lowStockProductsCount,
        List<LowStockAlert> lowStockAlerts
) {
    public record LowStockAlert(
            Long productId,
            String productName,
            String sku,
            int currentStock,
            int minimumStockLevel,
            String statusMessage
    ) {}
}