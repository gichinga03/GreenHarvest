package com.greenharvest.purchase.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record PurchaseRequest(
        @NotBlank(message = "Invoice number is required")
        String invoiceNumber,

        @NotNull(message = "Supplier ID is required")
        Long supplierId,

        @NotEmpty(message = "Purchase must contain at least one item")
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

            @NotNull(message = "Unit price is required")
            @Positive(message = "Unit price must be positive")
            BigDecimal unitPrice
    ) {}
}