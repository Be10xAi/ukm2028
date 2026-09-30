package com.tech10x.ukm.serviceimpl;

import com.tech10x.ukm.dto.request.PropertyDocumentRequest;
import com.tech10x.ukm.dto.request.PropertyImageRequest;
import com.tech10x.ukm.dto.request.PropertyRequest;
import com.tech10x.ukm.dto.response.*;
import com.tech10x.ukm.entity.*;
import com.tech10x.ukm.exception.ApiException;
import com.tech10x.ukm.repositoryproxy.CatalogProxyRepository;
import com.tech10x.ukm.repositoryproxy.PropertyProxyRepository;
import com.tech10x.ukm.repositoryproxy.RoomProxyRepository;
import com.tech10x.ukm.repositoryproxy.SupplierProxyRepository;
import com.tech10x.ukm.security.Actor;
import com.tech10x.ukm.security.PropertyAccessGuard;
import com.tech10x.ukm.security.SafeUrlPolicy;
import com.tech10x.ukm.service.AuditService;
import com.tech10x.ukm.service.PropertyService;
import com.tech10x.ukm.utils.DateTimeUtil;
import com.tech10x.ukm.utils.TextSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    /** Hard caps so one account cannot fill the database or the storage account. */
    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_IMAGES_PER_PROPERTY = 30;
    private static final int MAX_DOCUMENTS_PER_PROPERTY = 20;

    private final PropertyProxyRepository properties;
    private final CatalogProxyRepository catalog;
    private final RoomProxyRepository rooms;
    private final SupplierProxyRepository suppliers;
    private final PropertyAccessGuard guard;
    private final SafeUrlPolicy urlPolicy;
    private final AuditService audit;

    // ================================================================ property

    @Override
    @Transactional
    public PropertyResponse create(Actor actor, PropertyRequest request) {
        // The owner is ALWAYS the logged-in supplier - it is never read from the request.
        Supplier supplier = suppliers.findByUserId(actor.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "Only suppliers can create properties"));
        if (supplier.getVerificationStatus() == VerificationStatus.REJECTED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Your supplier account was rejected; please contact support");
        }

        Property property = Property.builder().supplier(supplier).build(); // status DRAFT, UNVERIFIED by default
        apply(property, request);
        return toResponse(properties.save(property));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertySummaryResponse> listMine(Actor actor, int page, int size) {
        Supplier supplier = suppliers.findByUserId(actor.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No supplier profile for this account"));
        return summaries(properties.findBySupplier(supplier.getId(), pageable(page, size)).getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertySummaryResponse> listAll(PropertyStatus status, int page, int size) {
        Pageable pageable = pageable(page, size);
        var result = status == null ? properties.findAll(pageable) : properties.findByStatus(status, pageable);
        return summaries(result.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyResponse get(Actor actor, String propertyId) {
        return toResponse(guard.loadForRead(actor, propertyId));
    }

    @Override
    @Transactional
    public PropertyResponse update(Actor actor, String propertyId, PropertyRequest request) {
        Property property = guard.loadForWrite(actor, propertyId);
        apply(property, request);
        touch(property);
        Property saved = properties.save(property);
        if (actor.admin()) {
            audit.record(actor.userId(), "PROPERTY", propertyId, AuditAction.UPDATE, null,
                    Map.of("name", saved.getName()));
        }
        return toResponse(saved);
    }

    @Override
    @Transactional
    public PropertyResponse setAmenities(Actor actor, String propertyId, Set<Integer> amenityIds) {
        Property property = guard.loadForWrite(actor, propertyId);
        if (amenityIds.contains(null)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "amenityIds must not contain null");
        }
        List<Amenity> found = catalog.findAmenitiesByIds(amenityIds);
        if (found.size() != amenityIds.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "One or more amenityIds do not exist");
        }
        property.getAmenities().clear();
        property.getAmenities().addAll(found);
        touch(property);
        return toResponse(properties.save(property));
    }

    // ================================================================ admin workflow

    @Override
    @Transactional
    public PropertyResponse updateStatus(Actor actor, String propertyId, PropertyStatus status) {
        requireAdmin(actor);
        Property property = guard.loadForRead(actor, propertyId);
        PropertyStatus before = property.getStatus();

        if (status == PropertyStatus.LIVE && before != PropertyStatus.LIVE) {
            // A listing may only go public once it is complete and its owner has been vetted.
            if (property.getSupplier().getVerificationStatus() != VerificationStatus.VERIFIED) {
                throw new ApiException(HttpStatus.CONFLICT, "The supplier is not verified yet");
            }
            if (properties.countImages(property.getId()) == 0) {
                throw new ApiException(HttpStatus.CONFLICT, "Add at least one image before publishing");
            }
            if (rooms.countRoomTypes(property.getId()) == 0) {
                throw new ApiException(HttpStatus.CONFLICT, "Add at least one room type before publishing");
            }
        }

        property.setStatus(status);
        touch(property);
        Property saved = properties.save(property);
        audit.record(actor.userId(), "PROPERTY", propertyId, AuditAction.STATUS_CHANGE,
                Map.of("status", before.name()), Map.of("status", status.name()));
        return toResponse(saved);
    }

    @Override
    @Transactional
    public PropertyResponse updateVerification(Actor actor, String propertyId, PropertyVerifiedStatus verifiedStatus) {
        requireAdmin(actor);
        Property property = guard.loadForRead(actor, propertyId);
        PropertyVerifiedStatus before = property.getVerifiedStatus();
        property.setVerifiedStatus(verifiedStatus);
        if (verifiedStatus == PropertyVerifiedStatus.VERIFIED) {
            property.setLastVerifiedDate(DateTimeUtil.todayIst());
        }
        touch(property);
        Property saved = properties.save(property);
        audit.record(actor.userId(), "PROPERTY", propertyId, AuditAction.STATUS_CHANGE,
                Map.of("verifiedStatus", before.name()), Map.of("verifiedStatus", verifiedStatus.name()));
        return toResponse(saved);
    }

    // ================================================================ images

    @Override
    @Transactional
    public PropertyImageResponse addImage(Actor actor, String propertyId, PropertyImageRequest request) {
        Property property = guard.loadForWrite(actor, propertyId);
        urlPolicy.requireAllowed(request.getUrl());

        long existing = properties.countImages(property.getId());
        if (existing >= MAX_IMAGES_PER_PROPERTY) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "A property can have at most " + MAX_IMAGES_PER_PROPERTY + " images");
        }

        boolean cover = existing == 0 || Boolean.TRUE.equals(request.getCover());
        if (cover) {
            properties.findImages(property.getId()).forEach(i -> i.setCover(false));
        }
        PropertyImage image = properties.saveImage(PropertyImage.builder()
                .property(property)
                .url(request.getUrl())
                .cover(cover)
                .build());
        return PropertyImageResponse.from(image);
    }

    @Override
    @Transactional
    public List<PropertyImageResponse> setCoverImage(Actor actor, String propertyId, String imageId) {
        Property property = guard.loadForWrite(actor, propertyId);
        List<PropertyImage> images = properties.findImages(property.getId());
        if (images.stream().noneMatch(i -> i.getImageId().equals(imageId))) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Image not found");
        }
        images.forEach(i -> i.setCover(i.getImageId().equals(imageId)));
        return properties.findImages(property.getId()).stream().map(PropertyImageResponse::from).toList();
    }

    @Override
    @Transactional
    public void deleteImage(Actor actor, String propertyId, String imageId) {
        Property property = guard.loadForWrite(actor, propertyId);
        PropertyImage image = properties.findImage(imageId, property.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Image not found"));
        boolean wasCover = image.isCover();
        properties.deleteImage(image);

        if (wasCover) { // keep exactly one cover while any image remains
            properties.findImages(property.getId()).stream()
                    .filter(i -> !i.getImageId().equals(imageId))
                    .findFirst()
                    .ifPresent(i -> i.setCover(true));
        }
    }

    // ================================================================ documents

    @Override
    @Transactional
    public PropertyDocumentResponse addDocument(Actor actor, String propertyId, PropertyDocumentRequest request) {
        Property property = guard.loadForWrite(actor, propertyId);
        urlPolicy.requireAllowed(request.getFileUrl());
        if (properties.countDocuments(property.getId()) >= MAX_DOCUMENTS_PER_PROPERTY) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "A property can have at most " + MAX_DOCUMENTS_PER_PROPERTY + " documents");
        }
        PropertyDocument document = properties.saveDocument(PropertyDocument.builder()
                .property(property)
                .documentType(request.getDocumentType())
                .fileUrl(request.getFileUrl())
                .verificationStatus(VerificationStatus.PENDING) // a supplier can never self-verify
                .build());
        return PropertyDocumentResponse.from(document);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyDocumentResponse> listDocuments(Actor actor, String propertyId) {
        Property property = guard.loadForRead(actor, propertyId);
        return properties.findDocuments(property.getId()).stream().map(PropertyDocumentResponse::from).toList();
    }

    @Override
    @Transactional
    public void deleteDocument(Actor actor, String propertyId, String documentId) {
        Property property = guard.loadForWrite(actor, propertyId);
        PropertyDocument document = properties.findDocument(documentId, property.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Document not found"));
        // Once an admin has verified a document it is evidence; only an admin may remove it.
        if (!actor.admin() && document.getVerificationStatus() == VerificationStatus.VERIFIED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "A verified document can only be removed by an admin");
        }
        properties.deleteDocument(document);
        audit.record(actor.userId(), "PROPERTY_DOCUMENT", documentId, AuditAction.DELETE,
                Map.of("documentType", document.getDocumentType().name(),
                        "verificationStatus", document.getVerificationStatus().name()), null);
    }

    @Override
    @Transactional
    public PropertyDocumentResponse verifyDocument(Actor actor, String propertyId, String documentId,
                                                   VerificationStatus status) {
        requireAdmin(actor);
        Property property = guard.loadForRead(actor, propertyId);
        PropertyDocument document = properties.findDocument(documentId, property.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Document not found"));
        VerificationStatus before = document.getVerificationStatus();
        document.setVerificationStatus(status);
        PropertyDocument saved = properties.saveDocument(document);
        audit.record(actor.userId(), "PROPERTY_DOCUMENT", documentId, AuditAction.STATUS_CHANGE,
                Map.of("verificationStatus", before.name()), Map.of("verificationStatus", status.name()));
        return PropertyDocumentResponse.from(saved);
    }

    // ================================================================ public

    @Override
    @Transactional(readOnly = true)
    public List<PropertySummaryResponse> searchPublic(Integer categoryId, Integer locationId, int page, int size) {
        // LIVE is fixed here - the public can never ask for DRAFT or SUSPENDED listings.
        return summaries(properties
                .searchByStatus(PropertyStatus.LIVE, categoryId, locationId, pageable(page, size))
                .getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public PublicPropertyResponse getPublic(String propertyId) {
        Property p = properties.findByPropertyId(propertyId)
                .filter(x -> x.getStatus() == PropertyStatus.LIVE)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Property not found"));

        return PublicPropertyResponse.builder()
                .propertyId(p.getPropertyId())
                .category(CategoryResponse.from(p.getCategory()))
                .location(LocationResponse.from(p.getLocation()))
                .name(p.getName())
                .description(p.getDescription())
                .address(p.getAddress())
                .latitude(p.getLatitude())
                .longitude(p.getLongitude())
                .checkInTime(p.getCheckInTime())
                .checkOutTime(p.getCheckOutTime())
                .cancellationPolicy(p.getCancellationPolicy())
                .verifiedStatus(p.getVerifiedStatus())
                .lastVerifiedDate(p.getLastVerifiedDate())
                .images(images(p))
                .amenities(amenities(p))
                .roomTypes(rooms.findRoomTypes(p.getId()).stream().map(RoomTypeResponse::from).toList())
                .build();
    }

    // ================================================================ helpers

    /** Copies the caller-editable fields; status / verification / owner are never touched here. */
    private void apply(Property property, PropertyRequest request) {
        String name = TextSanitizer.plain(request.getName());
        String address = TextSanitizer.plain(request.getAddress());
        if (name == null || address == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "name and address are required");
        }
        property.setCategory(catalog.findCategory(request.getCategoryId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "categoryId does not exist")));
        property.setLocation(catalog.findLocation(request.getLocationId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "locationId does not exist")));
        property.setName(name);
        property.setDescription(TextSanitizer.plain(request.getDescription()));
        property.setAddress(address);
        property.setLatitude(request.getLatitude());
        property.setLongitude(request.getLongitude());
        property.setCheckInTime(request.getCheckInTime());
        property.setCheckOutTime(request.getCheckOutTime());
        property.setCancellationPolicy(TextSanitizer.plain(request.getCancellationPolicy()));
    }

    /** Keeps updatedAt correct in the response we return (the JPA callback only runs at flush time). */
    private static void touch(Property property) {
        property.setUpdatedAt(LocalDateTime.now());
    }

    private static void requireAdmin(Actor actor) {
        if (!actor.admin()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }

    /** Page size is capped and the sort is fixed - callers cannot pick a sort column. */
    private static Pageable pageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
    }

    private List<PropertyImageResponse> images(Property p) {
        return properties.findImages(p.getId()).stream().map(PropertyImageResponse::from).toList();
    }

    private static List<AmenityResponse> amenities(Property p) {
        return p.getAmenities().stream()
                .sorted(Comparator.comparing(Amenity::getName))
                .map(AmenityResponse::from)
                .toList();
    }

    private PropertyResponse toResponse(Property p) {
        return PropertyResponse.builder()
                .propertyId(p.getPropertyId())
                .supplierId(p.getSupplier().getSupplierId())
                .category(CategoryResponse.from(p.getCategory()))
                .location(LocationResponse.from(p.getLocation()))
                .name(p.getName())
                .description(p.getDescription())
                .address(p.getAddress())
                .latitude(p.getLatitude())
                .longitude(p.getLongitude())
                .checkInTime(p.getCheckInTime())
                .checkOutTime(p.getCheckOutTime())
                .cancellationPolicy(p.getCancellationPolicy())
                .verifiedStatus(p.getVerifiedStatus())
                .lastVerifiedDate(p.getLastVerifiedDate())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .images(images(p))
                .amenities(amenities(p))
                .build();
    }

    /** Builds list cards with two batched queries (cover images, prices) instead of two per row. */
    private List<PropertySummaryResponse> summaries(List<Property> list) {
        if (list.isEmpty()) {
            return List.of();
        }
        List<Long> ids = list.stream().map(Property::getId).toList();
        Map<Long, String> covers = properties.findCoverImages(ids).stream()
                .collect(Collectors.toMap(i -> i.getProperty().getId(), PropertyImage::getUrl, (a, b) -> a));
        Map<Long, BigDecimal> prices = rooms.startingPrices(ids);

        return list.stream().map(p -> PropertySummaryResponse.builder()
                .propertyId(p.getPropertyId())
                .name(p.getName())
                .categoryName(p.getCategory().getName())
                .city(p.getLocation().getCity())
                .areaLandmark(p.getLocation().getAreaLandmark())
                .coverImageUrl(covers.get(p.getId()))
                .startingPrice(prices.get(p.getId()))
                .verifiedStatus(p.getVerifiedStatus())
                .status(p.getStatus())
                .build()).toList();
    }
}
