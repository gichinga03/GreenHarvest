package com.greenharvest.adjustment.repository;

import com.greenharvest.adjustment.model.InventoryAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryAdjustmentRepository extends JpaRepository<InventoryAdjustment, Long> {

    // Retrieve all adjustments for a specific product, ordered by newest first
    List<InventoryAdjustment> findByProductIdOrderByCreatedAtDesc(Long productId);

    // Retrieve all adjustments made by a specific user
    List<InventoryAdjustment> findByAdjustedByIdOrderByCreatedAtDesc(Long userId);
}