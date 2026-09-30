package com.tech10x.ukm.controller;

import com.tech10x.ukm.dto.response.*;
import com.tech10x.ukm.service.CatalogService;
import com.tech10x.ukm.service.PropertyService;
import com.tech10x.ukm.service.RoomService;
import com.tech10x.ukm.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Anonymous, READ-ONLY browsing for the public website. Only GET is permitted for /public/**
 * in SecurityConfig, and every response here is a public-safe DTO for LIVE properties only.
 */
@RestController
@RequestMapping("/api/v1/ukm/public")
@RequiredArgsConstructor
public class PublicCatalogController {

    private final CatalogService catalogService;
    private final PropertyService propertyService;
    private final RoomService roomService;

    @GetMapping("/categories")
    public ResponseEntity<GenericResponse<CategoryResponse>> categories() {
        return ResponseEntity.ok(ResponseUtil.success(catalogService.listCategories()));
    }

    @GetMapping("/locations")
    public ResponseEntity<GenericResponse<LocationResponse>> locations() {
        return ResponseEntity.ok(ResponseUtil.success(catalogService.listLocations()));
    }

    @GetMapping("/amenities")
    public ResponseEntity<GenericResponse<AmenityResponse>> amenities() {
        return ResponseEntity.ok(ResponseUtil.success(catalogService.listAmenities()));
    }

    /** Search LIVE properties, optionally by ?categoryId= and ?locationId=. Page size is capped at 50. */
    @GetMapping("/properties")
    public ResponseEntity<GenericResponse<PropertySummaryResponse>> search(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer locationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ResponseUtil.success(
                propertyService.searchPublic(categoryId, locationId, page, size)));
    }

    @GetMapping("/properties/{propertyId}")
    public ResponseEntity<GenericResponse<PublicPropertyResponse>> get(@PathVariable String propertyId) {
        return ResponseEntity.ok(ResponseUtil.success(List.of(propertyService.getPublic(propertyId))));
    }

    /** Date-wise availability for a room type, max 62 days per request. */
    @GetMapping("/properties/{propertyId}/room-types/{roomTypeId}/availability")
    public ResponseEntity<GenericResponse<PublicInventoryResponse>> availability(
            @PathVariable String propertyId,
            @PathVariable String roomTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ResponseUtil.success(
                roomService.getPublicInventory(propertyId, roomTypeId, from, to)));
    }
}
