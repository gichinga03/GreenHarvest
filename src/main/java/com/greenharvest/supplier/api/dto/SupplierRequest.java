package com.greenharvest.supplier.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplierRequest(
        @NotBlank(message = "Supplier name is required")
        @Size(max = 150, message = "Supplier name cannot exceed 150 characters")
        String name,

        @Size(max = 100, message = "Contact person name cannot exceed 100 characters")
        String contactPerson,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 150, message = "Email cannot exceed 150 characters")
        String email,

        @NotBlank(message = "Phone number is required")
        @Size(max = 20, message = "Phone number cannot exceed 20 characters")
        String phone,

        @Size(max = 255, message = "Address cannot exceed 255 characters")
        String address
) {}