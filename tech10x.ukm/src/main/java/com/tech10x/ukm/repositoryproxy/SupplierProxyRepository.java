package com.tech10x.ukm.repositoryproxy;

import com.tech10x.ukm.entity.Supplier;
import com.tech10x.ukm.entity.VerificationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SupplierProxyRepository {

    private final com.tech10x.ukm.repository.SupplierRepository supplierRepository;

    public Optional<Supplier> findByUserId(String userId) {
        return supplierRepository.findByUser_UserId(userId);
    }

    public boolean existsByUserId(String userId) {
        return supplierRepository.existsByUser_UserId(userId);
    }

    public Optional<Supplier> findBySupplierId(String supplierId) {
        return supplierRepository.findBySupplierId(supplierId);
    }

    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    public List<Supplier> findByVerificationStatus(VerificationStatus status) {
        return supplierRepository.findByVerificationStatus(status);
    }

    public Supplier save(Supplier supplier) {
        return supplierRepository.save(supplier);
    }
}
