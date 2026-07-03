package com.greenharvest.supplier.api.dto;

import java.time.LocalDateTime;

public record SupplierResponse(
        Long id,
        String name,
        String contactPerson,
        String email,
        String phone,
        String address,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}