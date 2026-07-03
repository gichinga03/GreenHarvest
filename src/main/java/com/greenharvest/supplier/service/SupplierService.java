package com.greenharvest.supplier.service;

import com.greenharvest.common.exception.DuplicateResourceException;
import com.greenharvest.common.exception.ResourceNotFoundException;
import com.greenharvest.supplier.api.dto.SupplierRequest;
import com.greenharvest.supplier.api.dto.SupplierResponse;
import com.greenharvest.supplier.model.Supplier;
import com.greenharvest.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        if (supplierRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Supplier email already registered: " + request.email());
        }

        Supplier supplier = Supplier.builder()
                .name(request.name().trim())
                .contactPerson(request.contactPerson() != null ? request.contactPerson().trim() : null)
                .email(request.email().toLowerCase().trim())
                .phone(request.phone().trim())
                .address(request.address())
                .active(true)
                .build();

        return mapToResponse(supplierRepository.save(supplier));
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        return mapToResponse(supplier);
    }

    @Transactional
    public SupplierResponse updateSupplier(Long id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        if (!supplier.getEmail().equalsIgnoreCase(request.email()) && supplierRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email is already claimed by another supplier: " + request.email());
        }

        supplier.setName(request.name().trim());
        supplier.setContactPerson(request.contactPerson());
        supplier.setEmail(request.email().toLowerCase().trim());
        supplier.setPhone(request.phone().trim());
        supplier.setAddress(request.address());

        return mapToResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierResponse toggleSupplierStatus(Long id, boolean active) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        supplier.setActive(active);
        return mapToResponse(supplierRepository.save(supplier));
    }

    private SupplierResponse mapToResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getContactPerson(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getAddress(),
                supplier.isActive(),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt()
        );
    }
}