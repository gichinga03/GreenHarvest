package com.greenharvest.sale.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record SaleRequest(
        @NotBlank(message = "Customer name is required")
        @Size(max = 150, message = "Customer name cannot exceed 150 characters")
        String customerName,

        @NotEmpty(message = "Sale must contain at least one item")
        @Valid
        List<ItemRequest> items,

        String remarks
) {
    public record ItemRequest(
            @NotNull(message = "Product ID is required")
            Long productId,

            @NotNull(message = "Quantity is required")
            @Min(value = 1, message = "Quantity must be at least 1")
            Integer quantity,

            // 💡 unitPrice is no longer mandatory; falls back to catalog price if null
            @Positive(message = "Unit price must be positive if specified")
            BigDecimal unitPrice
    ) {}
}