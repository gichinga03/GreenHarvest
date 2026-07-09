package com.greenharvest.supplier.api.controller;

import com.greenharvest.supplier.api.dto.SupplierRequest;
import com.greenharvest.supplier.api.dto.SupplierResponse;
import com.greenharvest.supplier.service.SupplierService;
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
    public ResponseEntity<SupplierResponse> createSupplier(@RequestBody SupplierRequest request) {
        SupplierResponse response = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()") // Any authenticated employee can check supplier details
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers() {
        List<SupplierResponse> response = supplierService.getAllSuppliers();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SupplierResponse> getSupplierById(@PathVariable Long id) {
        SupplierResponse response = supplierService.getSupplierById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATOR', 'ROLE_WAREHOUSE_OFFICER')")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable Long id,
            @RequestBody SupplierRequest request) {
        SupplierResponse response = supplierService.updateSupplier(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRATOR')") // Changing status or suspending vendors is an Admin task
    public ResponseEntity<SupplierResponse> toggleSupplierStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        SupplierResponse response = supplierService.toggleSupplierStatus(id, active);
        return ResponseEntity.ok(response);
    }
}