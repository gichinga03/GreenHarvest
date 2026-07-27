package com.greenharvest.adjustment.api.controller;

import com.greenharvest.adjustment.api.dto.CreateAdjustmentRequest;
import com.greenharvest.adjustment.api.dto.InventoryAdjustmentResponse;
import com.greenharvest.adjustment.service.InventoryAdjustmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/adjustments")
@RequiredArgsConstructor
public class InventoryAdjustmentController {

    private final InventoryAdjustmentService adjustmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'WAREHOUSE_OFFICER')")
    public ResponseEntity<InventoryAdjustmentResponse> createAdjustment(
            @RequestBody CreateAdjustmentRequest request
    ) {
        InventoryAdjustmentResponse response = adjustmentService.createAdjustment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'WAREHOUSE_OFFICER')")
    public ResponseEntity<List<InventoryAdjustmentResponse>> getAllAdjustments() {
        List<InventoryAdjustmentResponse> responses = adjustmentService.getAllAdjustments();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'WAREHOUSE_OFFICER')")
    public ResponseEntity<List<InventoryAdjustmentResponse>> getAdjustmentsByProduct(
            @PathVariable Long productId
    ) {
        List<InventoryAdjustmentResponse> responses = adjustmentService.getAdjustmentsByProduct(productId);
        return ResponseEntity.ok(responses);
    }
}