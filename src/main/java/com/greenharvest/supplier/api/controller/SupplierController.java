package com.greenharvest.supplier.api.controller;

import com.greenharvest.common.response.ApiResponse;
import com.greenharvest.supplier.api.dto.SupplierRequest;
import com.greenharvest.supplier.api.dto.SupplierResponse;
import com.greenharvest.supplier.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATOR', 'ROLE_WAREHOUSE_OFFICER')")
    public ResponseEntity<ApiResponse<SupplierResponse>> createSupplier(@Valid @RequestBody SupplierRequest request) {
        SupplierResponse response = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Supplier registered successfully", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()") // Any authenticated employee can check supplier details
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> getAllSuppliers() {
        List<SupplierResponse> response = supplierService.getAllSuppliers();
        return ResponseEntity.ok(ApiResponse.success("Suppliers list retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SupplierResponse>> getSupplierById(@PathVariable Long id) {
        SupplierResponse response = supplierService.getSupplierById(id);
        return ResponseEntity.ok(ApiResponse.success("Supplier details retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATOR', 'ROLE_WAREHOUSE_OFFICER')")
    public ResponseEntity<ApiResponse<SupplierResponse>> updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequest request) {
        SupplierResponse response = supplierService.updateSupplier(id, request);
        return ResponseEntity.ok(ApiResponse.success("Supplier profiles updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRATOR')") // Changing status or suspending vendors is an Admin task
    public ResponseEntity<ApiResponse<SupplierResponse>> toggleSupplierStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        SupplierResponse response = supplierService.toggleSupplierStatus(id, active);
        String action = active ? "activated" : "deactivated";
        return ResponseEntity.ok(ApiResponse.success("Supplier profile successfully " + action, response));
    }
}