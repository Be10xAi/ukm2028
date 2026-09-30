package com.tech10x.ukm.repository;

import com.tech10x.ukm.entity.Property;
import com.tech10x.ukm.entity.PropertyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    Optional<Property> findByPropertyId(String propertyId);

    Page<Property> findBySupplier_Id(Long supplierDbId, Pageable pageable);

    Page<Property> findByStatus(PropertyStatus status, Pageable pageable);

    /** Public search - the status is always passed in by the service (LIVE), never by the caller. */
    @Query("select p from Property p where p.status = :status "
            + "and (:categoryId is null or p.category.id = :categoryId) "
            + "and (:locationId is null or p.location.id = :locationId)")
    Page<Property> searchByStatus(@Param("status") PropertyStatus status,
                                  @Param("categoryId") Integer categoryId,
                                  @Param("locationId") Integer locationId,
                                  Pageable pageable);
}
