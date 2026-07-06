package com.greenharvest.sale.api.controller;

import com.greenharvest.common.response.ApiResponse;
import com.greenharvest.sale.api.dto.SaleRequest;
import com.greenharvest.sale.api.dto.SaleResponse;
import com.greenharvest.sale.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATOR', 'ROLE_SALES_OFFICER')")
    public ResponseEntity<ApiResponse<SaleResponse>> recordSale(@Valid @RequestBody SaleRequest request) {
        SaleResponse response = saleService.recordSale(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Outbound sales transaction processed successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATOR', 'ROLE_SALES_OFFICER')")
    public ResponseEntity<ApiResponse<List<SaleResponse>>> getAllSales() {
        List<SaleResponse> response = saleService.getAllSales();
        return ResponseEntity.ok(ApiResponse.success("Sales historical logs retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATOR', 'ROLE_SALES_OFFICER')")
    public ResponseEntity<ApiResponse<SaleResponse>> getSaleById(@PathVariable Long id) {
        SaleResponse response = saleService.getSaleById(id);
        return ResponseEntity.ok(ApiResponse.success("Outbound transaction details resolved", response));
    }
}