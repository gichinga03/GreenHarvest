package com.greenharvest.supplier.api.dto;

public record SupplierRequest(
        String name,
        String contactPerson,
        String email,
        String phone,
        String address
) {}