package com.greenharvest.adjustment.api.dto;

import com.greenharvest.adjustment.model.AdjustmentType;

import java.time.LocalDateTime;

public record InventoryAdjustmentResponse(
        Long id,
        Long productId,
        String productName,
        String productSku,
        AdjustmentType type,
        Integer quantity,
        String reason,
        Long userId,
        String userName,
        LocalDateTime createdAt
) {}