package com.greenharvest.supplier.service;

import com.greenharvest.common.exception.DuplicateResourceException;
import com.greenharvest.common.exception.ResourceNotFoundException;
import com.greenharvest.common.exception.ValidationException;
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
        //  MANUAL DATA VALIDATION (Requirement 7)
        validateSupplierRequest(request);

        String normalizedEmail = request.email().toLowerCase().trim();
        if (supplierRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("Supplier email already registered: " + request.email());
        }

        Supplier supplier = Supplier.builder()
                .name(request.name().trim())
                .contactPerson(request.contactPerson() != null ? request.contactPerson().trim() : null)
                .email(normalizedEmail)
                .phone(request.phone().trim())
                .address(request.address() != null ? request.address().trim() : null)
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

        //  MANUAL DATA VALIDATION (Requirement 7)
        validateSupplierRequest(request);

        String normalizedEmail = request.email().toLowerCase().trim();

        if (!supplier.getEmail().equalsIgnoreCase(normalizedEmail) && supplierRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("Email is already claimed by another supplier: " + request.email());
        }

        supplier.setName(request.name().trim());
        supplier.setContactPerson(request.contactPerson() != null ? request.contactPerson().trim() : null);
        supplier.setEmail(normalizedEmail);
        supplier.setPhone(request.phone().trim());
        supplier.setAddress(request.address() != null ? request.address().trim() : null);

        return mapToResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierResponse toggleSupplierStatus(Long id, boolean active) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        supplier.setActive(active);
        return mapToResponse(supplierRepository.save(supplier));
    }

    private void validateSupplierRequest(SupplierRequest request) {
        if (request.name() == null || request.name().trim().isBlank()) {
            throw new ValidationException("Supplier name is required");
        }
        if (request.email() == null || request.email().trim().isBlank()) {
            throw new ValidationException("Supplier email is required");
        }
        if (!request.email().contains("@") || !request.email().contains(".")) {
            throw new ValidationException("Invalid email formatting");
        }
        if (request.phone() == null || request.phone().trim().isBlank()) {
            throw new ValidationException("Phone number is required");
        }
        // Match standard formatting patterns for phone contacts (10 to 15 digits)
        if (!request.phone().trim().matches("^\\+?[0-9]{10,15}$")) {
            throw new ValidationException("Phone number must be valid (between 10 to 15 digits, e.g., +254711223344)");
        }
        if (request.address() == null || request.address().trim().isBlank()) {
            throw new ValidationException("Physical address is required");
        }
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