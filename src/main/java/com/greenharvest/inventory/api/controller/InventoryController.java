package com.greenharvest.inventory.api.controller;

import com.greenharvest.common.response.ApiResponse;
import com.greenharvest.inventory.api.dto.InventoryDashboardResponse;
import com.greenharvest.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATOR', 'ROLE_WAREHOUSE_OFFICER')")
    public ResponseEntity<ApiResponse<InventoryDashboardResponse>> getInventoryDashboard() {
        InventoryDashboardResponse response = inventoryService.getDashboardMetrics();
        return ResponseEntity.ok(ApiResponse.success("Aggregated inventory status dashboard loaded", response));
    }
}