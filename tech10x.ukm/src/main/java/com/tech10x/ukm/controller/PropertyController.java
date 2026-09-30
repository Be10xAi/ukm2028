package com.tech10x.ukm.controller;

import com.tech10x.ukm.dto.request.*;
import com.tech10x.ukm.dto.response.*;
import com.tech10x.ukm.entity.PropertyStatus;
import com.tech10x.ukm.security.Actor;
import com.tech10x.ukm.service.PropertyService;
import com.tech10x.ukm.utils.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Property management for suppliers (own properties only) and admins (all properties).
 * Role checks are here; ownership checks are enforced in the service via PropertyAccessGuard.
 * The acting user always comes from the JWT (Authentication), never from the request.
 */
@RestController
@RequestMapping("/api/v1/ukm/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    // ------------------------------------------------------------ property

    /** Supplier: create a property; it starts as DRAFT / UNVERIFIED and is owned by the caller. */
    @PostMapping
    @PreAuthorize("hasRole('SUPPLIER')")
    public ResponseEntity<GenericResponse<PropertyResponse>> create(
            Authentication authentication, @Valid @RequestBody PropertyRequest request) {
        PropertyResponse created = propertyService.create(Actor.of(authentication), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success(List.of(created), "Property created"));
    }

    /** Supplier: my properties. */
    @GetMapping("/mine")
    @PreAuthorize("hasRole('SUPPLIER')")
    public ResponseEntity<GenericResponse<PropertySummaryResponse>> mine(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ResponseUtil.success(
                propertyService.listMine(Actor.of(authentication), page, size)));
    }

    /** Admin: all properties, optionally filtered by ?status=DRAFT|LIVE|SUSPENDED. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GenericResponse<PropertySummaryResponse>> listAll(
            @RequestParam(required = false) PropertyStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ResponseUtil.success(propertyService.listAll(status, page, size)));
    }

    @GetMapping("/{propertyId}")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<PropertyResponse>> get(
            Authentication authentication, @PathVariable String propertyId) {
        return ResponseEntity.ok(ResponseUtil.success(
                List.of(propertyService.get(Actor.of(authentication), propertyId))));
    }

    @PutMapping("/{propertyId}")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<PropertyResponse>> update(
            Authentication authentication, @PathVariable String propertyId,
            @Valid @RequestBody PropertyRequest request) {
        return ResponseEntity.ok(ResponseUtil.success(
                List.of(propertyService.update(Actor.of(authentication), propertyId, request)),
                "Property updated"));
    }

    @PutMapping("/{propertyId}/amenities")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<PropertyResponse>> setAmenities(
            Authentication authentication, @PathVariable String propertyId,
            @Valid @RequestBody PropertyAmenitiesRequest request) {
        return ResponseEntity.ok(ResponseUtil.success(
                List.of(propertyService.setAmenities(Actor.of(authentication), propertyId, request.getAmenityIds())),
                "Amenities updated"));
    }

    // ------------------------------------------------------------ admin workflow

    /** Admin: publish (LIVE), take down (SUSPENDED) or reset (DRAFT) a property. */
    @PatchMapping("/{propertyId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GenericResponse<PropertyResponse>> updateStatus(
            Authentication authentication, @PathVariable String propertyId,
            @Valid @RequestBody PropertyStatusRequest request) {
        return ResponseEntity.ok(ResponseUtil.success(
                List.of(propertyService.updateStatus(Actor.of(authentication), propertyId, request.getStatus())),
                "Property status updated"));
    }

    /** Admin: mark a property VERIFIED / UNVERIFIED. */
    @PatchMapping("/{propertyId}/verification")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GenericResponse<PropertyResponse>> updateVerification(
            Authentication authentication, @PathVariable String propertyId,
            @Valid @RequestBody PropertyVerificationRequest request) {
        return ResponseEntity.ok(ResponseUtil.success(
                List.of(propertyService.updateVerification(
                        Actor.of(authentication), propertyId, request.getVerifiedStatus())),
                "Property verification updated"));
    }

    // ------------------------------------------------------------ images

    @PostMapping("/{propertyId}/images")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<PropertyImageResponse>> addImage(
            Authentication authentication, @PathVariable String propertyId,
            @Valid @RequestBody PropertyImageRequest request) {
        PropertyImageResponse image = propertyService.addImage(Actor.of(authentication), propertyId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success(List.of(image), "Image added"));
    }

    @PatchMapping("/{propertyId}/images/{imageId}/cover")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<PropertyImageResponse>> setCover(
            Authentication authentication, @PathVariable String propertyId, @PathVariable String imageId) {
        return ResponseEntity.ok(ResponseUtil.success(
                propertyService.setCoverImage(Actor.of(authentication), propertyId, imageId), "Cover image updated"));
    }

    @DeleteMapping("/{propertyId}/images/{imageId}")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<Object>> deleteImage(
            Authentication authentication, @PathVariable String propertyId, @PathVariable String imageId) {
        propertyService.deleteImage(Actor.of(authentication), propertyId, imageId);
        return ResponseEntity.ok(ResponseUtil.success("Image deleted"));
    }

    // ------------------------------------------------------------ documents (owner / admin only)

    @PostMapping("/{propertyId}/documents")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<PropertyDocumentResponse>> addDocument(
            Authentication authentication, @PathVariable String propertyId,
            @Valid @RequestBody PropertyDocumentRequest request) {
        PropertyDocumentResponse document = propertyService.addDocument(Actor.of(authentication), propertyId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success(List.of(document), "Document added"));
    }

    @GetMapping("/{propertyId}/documents")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<PropertyDocumentResponse>> listDocuments(
            Authentication authentication, @PathVariable String propertyId) {
        return ResponseEntity.ok(ResponseUtil.success(
                propertyService.listDocuments(Actor.of(authentication), propertyId)));
    }

    @DeleteMapping("/{propertyId}/documents/{documentId}")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<Object>> deleteDocument(
            Authentication authentication, @PathVariable String propertyId, @PathVariable String documentId) {
        propertyService.deleteDocument(Actor.of(authentication), propertyId, documentId);
        return ResponseEntity.ok(ResponseUtil.success("Document deleted"));
    }

    /** Admin: verify or reject a submitted document. */
    @PatchMapping("/{propertyId}/documents/{documentId}/verification")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GenericResponse<PropertyDocumentResponse>> verifyDocument(
            Authentication authentication, @PathVariable String propertyId, @PathVariable String documentId,
            @Valid @RequestBody DocumentVerificationRequest request) {
        return ResponseEntity.ok(ResponseUtil.success(
                List.of(propertyService.verifyDocument(
                        Actor.of(authentication), propertyId, documentId, request.getStatus())),
                "Document verification updated"));
    }
}
