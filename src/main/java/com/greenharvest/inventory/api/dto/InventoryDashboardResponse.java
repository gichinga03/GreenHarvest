package com.greenharvest.inventory.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record InventoryDashboardResponse(
        // 📊 Financial & Stock Summary Metrics
        long totalProductsCount,
        long totalStockQuantity,
        BigDecimal totalInventoryValue, // Evaluated via: SUM(currentStock * price)

        // 🚨 Operational Risk Assessment Metrics
        long lowStockProductsCount, // Products where currentStock <= minimumStockLevel
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