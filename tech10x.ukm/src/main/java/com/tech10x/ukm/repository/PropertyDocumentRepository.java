package com.tech10x.ukm.repository;

import com.tech10x.ukm.entity.PropertyDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyDocumentRepository extends JpaRepository<PropertyDocument, Long> {

    List<PropertyDocument> findByProperty_IdOrderByIdAsc(Long propertyDbId);

    Optional<PropertyDocument> findByDocumentIdAndProperty_Id(String documentId, Long propertyDbId);

    long countByProperty_Id(Long propertyDbId);
}
