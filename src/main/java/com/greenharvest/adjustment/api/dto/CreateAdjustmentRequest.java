package com.greenharvest.adjustment.api.dto;

import com.greenharvest.adjustment.model.AdjustmentType;

public record CreateAdjustmentRequest(
        Long productId,
        AdjustmentType type,
        Integer quantity,
        String reason
) {}