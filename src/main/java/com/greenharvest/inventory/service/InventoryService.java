package com.greenharvest.inventory.service;

import com.greenharvest.inventory.api.dto.InventoryDashboardResponse;
import com.greenharvest.product.model.Product;
import com.greenharvest.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public InventoryDashboardResponse getDashboardMetrics() {
        // Fetch the entire live inventory ledger line array
        List<Product> products = productRepository.findAll();

        //  TOTAL COUNTS CALCULATION
        long totalProductsCount = products.size();

        // TOTAL PHYSICAL VOLUME CALCULATION
        long totalStockQuantity = products.stream()
                .mapToLong(Product::getCurrentStock)
                .sum();

        //  TOTAL FINANCIAL VALUATION CALCULATION: Sum up (Stock * Selling Price)
        BigDecimal totalInventoryValue = products.stream()
                .map(product -> BigDecimal.valueOf(product.getCurrentStock()).multiply(product.getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //  LOW STOCK FILTERING CALCULATION
        List<InventoryDashboardResponse.LowStockAlert> lowStockAlerts = products.stream()
                .filter(product -> product.getCurrentStock() <= product.getMinimumStockLevel())
                .map(product -> new InventoryDashboardResponse.LowStockAlert(
                        product.getId(),
                        product.getName(),
                        product.getSku(),
                        product.getCurrentStock(),
                        product.getMinimumStockLevel(),
                        String.format("Stock critical! Requires replenishment. Deficit: %d units",
                                (product.getMinimumStockLevel() - product.getCurrentStock()))
                ))
                .collect(Collectors.toList());

        long lowStockProductsCount = lowStockAlerts.size();

        return new InventoryDashboardResponse(
                totalProductsCount,
                totalStockQuantity,
                totalInventoryValue,
                lowStockProductsCount,
                lowStockAlerts
        );
    }
}