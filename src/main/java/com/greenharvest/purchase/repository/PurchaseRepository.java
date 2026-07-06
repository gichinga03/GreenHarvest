package com.greenharvest.purchase.repository;

import com.greenharvest.purchase.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findBySupplierId(Long supplierId);
    boolean existsByInvoiceNumber(String invoiceNumber);
}