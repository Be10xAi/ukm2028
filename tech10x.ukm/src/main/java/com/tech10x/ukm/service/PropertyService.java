package com.tech10x.ukm.service;

import com.tech10x.ukm.dto.request.*;
import com.tech10x.ukm.dto.response.*;
import com.tech10x.ukm.entity.PropertyStatus;
import com.tech10x.ukm.entity.PropertyVerifiedStatus;
import com.tech10x.ukm.entity.VerificationStatus;
import com.tech10x.ukm.security.Actor;

import java.util.List;
import java.util.Set;

/**
 * PROPERTY and its images, documents and amenities.
 * Every method that takes an {@link Actor} enforces ownership: a supplier only ever reaches
 * their own properties; admins reach all of them.
 */
public interface PropertyService {

    // ---- supplier / admin ----
    PropertyResponse create(Actor actor, PropertyRequest request);

    List<PropertySummaryResponse> listMine(Actor actor, int page, int size);

    List<PropertySummaryResponse> listAll(PropertyStatus status, int page, int size);

    PropertyResponse get(Actor actor, String propertyId);

    PropertyResponse update(Actor actor, String propertyId, PropertyRequest request);

    PropertyResponse setAmenities(Actor actor, String propertyId, Set<Integer> amenityIds);

    // ---- admin only ----
    PropertyResponse updateStatus(Actor actor, String propertyId, PropertyStatus status);

    PropertyResponse updateVerification(Actor actor, String propertyId, PropertyVerifiedStatus verifiedStatus);

    // ---- images ----
    PropertyImageResponse addImage(Actor actor, String propertyId, PropertyImageRequest request);

    List<PropertyImageResponse> setCoverImage(Actor actor, String propertyId, String imageId);

    void deleteImage(Actor actor, String propertyId, String imageId);

    // ---- documents ----
    PropertyDocumentResponse addDocument(Actor actor, String propertyId, PropertyDocumentRequest request);

    List<PropertyDocumentResponse> listDocuments(Actor actor, String propertyId);

    void deleteDocument(Actor actor, String propertyId, String documentId);

    PropertyDocumentResponse verifyDocument(Actor actor, String propertyId, String documentId, VerificationStatus status);

    // ---- public (no login) ----
    List<PropertySummaryResponse> searchPublic(Integer categoryId, Integer locationId, int page, int size);

    PublicPropertyResponse getPublic(String propertyId);
}
