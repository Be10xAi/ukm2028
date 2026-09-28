package com.tech10x.ukm.repository;

import com.tech10x.ukm.entity.Supplier;
import com.tech10x.ukm.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    /** Navigates the FK: Supplier.user.userId. */
    Optional<Supplier> findByUser_UserId(String userId);

    boolean existsByUser_UserId(String userId);

    Optional<Supplier> findBySupplierId(String supplierId);

    List<Supplier> findByVerificationStatus(VerificationStatus status);
}
