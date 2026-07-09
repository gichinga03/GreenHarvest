package com.greenharvest.purchase.api.controller;

import com.greenharvest.purchase.api.dto.PurchaseRequest;
import com.greenharvest.purchase.api.dto.PurchaseResponse;
import com.greenharvest.purchase.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATOR', 'ROLE_WAREHOUSE_OFFICER')")
    public ResponseEntity<PurchaseResponse> recordPurchase(@RequestBody PurchaseRequest request) {
        PurchaseResponse response = purchaseService.recordPurchase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATOR', 'ROLE_WAREHOUSE_OFFICER')")
    public ResponseEntity<List<PurchaseResponse>> getAllPurchases() {
        List<PurchaseResponse> response = purchaseService.getAllPurchases();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATOR', 'ROLE_WAREHOUSE_OFFICER')")
    public ResponseEntity<PurchaseResponse> getPurchaseById(@PathVariable Long id) {
        PurchaseResponse response = purchaseService.getPurchaseById(id);
        return ResponseEntity.ok(response);
    }
}