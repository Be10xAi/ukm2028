package com.tech10x.ukm.repositoryproxy;

import com.tech10x.ukm.entity.Property;
import com.tech10x.ukm.entity.PropertyDocument;
import com.tech10x.ukm.entity.PropertyImage;
import com.tech10x.ukm.entity.PropertyStatus;
import com.tech10x.ukm.repository.PropertyDocumentRepository;
import com.tech10x.ukm.repository.PropertyImageRepository;
import com.tech10x.ukm.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** PROPERTY plus its PROPERTY_IMAGE and PROPERTY_DOCUMENT children. */
@Component
@RequiredArgsConstructor
public class PropertyProxyRepository {

    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository imageRepository;
    private final PropertyDocumentRepository documentRepository;

    // ---- property ----
    public Optional<Property> findByPropertyId(String propertyId) {
        return propertyRepository.findByPropertyId(propertyId);
    }

    public Property save(Property property) {
        return propertyRepository.save(property);
    }

    public Page<Property> findBySupplier(Long supplierDbId, Pageable pageable) {
        return propertyRepository.findBySupplier_Id(supplierDbId, pageable);
    }

    public Page<Property> findAll(Pageable pageable) {
        return propertyRepository.findAll(pageable);
    }

    public Page<Property> findByStatus(PropertyStatus status, Pageable pageable) {
        return propertyRepository.findByStatus(status, pageable);
    }

    public Page<Property> searchByStatus(PropertyStatus status, Integer categoryId, Integer locationId, Pageable pageable) {
        return propertyRepository.searchByStatus(status, categoryId, locationId, pageable);
    }

    // ---- images ----
    public List<PropertyImage> findImages(Long propertyDbId) {
        return imageRepository.findByProperty_IdOrderByCoverDescIdAsc(propertyDbId);
    }

    public Optional<PropertyImage> findImage(String imageId, Long propertyDbId) {
        return imageRepository.findByImageIdAndProperty_Id(imageId, propertyDbId);
    }

    public long countImages(Long propertyDbId) {
        return imageRepository.countByProperty_Id(propertyDbId);
    }

    public List<PropertyImage> findCoverImages(Collection<Long> propertyDbIds) {
        return imageRepository.findByProperty_IdInAndCoverTrue(propertyDbIds);
    }

    public PropertyImage saveImage(PropertyImage image) {
        return imageRepository.save(image);
    }

    public void deleteImage(PropertyImage image) {
        imageRepository.delete(image);
    }

    // ---- documents ----
    public List<PropertyDocument> findDocuments(Long propertyDbId) {
        return documentRepository.findByProperty_IdOrderByIdAsc(propertyDbId);
    }

    public Optional<PropertyDocument> findDocument(String documentId, Long propertyDbId) {
        return documentRepository.findByDocumentIdAndProperty_Id(documentId, propertyDbId);
    }

    public long countDocuments(Long propertyDbId) {
        return documentRepository.countByProperty_Id(propertyDbId);
    }

    public PropertyDocument saveDocument(PropertyDocument document) {
        return documentRepository.save(document);
    }

    public void deleteDocument(PropertyDocument document) {
        documentRepository.delete(document);
    }
}
