package com.tech10x.ukm.controller;

import com.tech10x.ukm.dto.request.AmenityRequest;
import com.tech10x.ukm.dto.request.CategoryRequest;
import com.tech10x.ukm.dto.request.LocationRequest;
import com.tech10x.ukm.dto.response.AmenityResponse;
import com.tech10x.ukm.dto.response.CategoryResponse;
import com.tech10x.ukm.dto.response.GenericResponse;
import com.tech10x.ukm.dto.response.LocationResponse;
import com.tech10x.ukm.security.Actor;
import com.tech10x.ukm.service.CatalogService;
import com.tech10x.ukm.utils.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Admin-only writes to the master data (categories, locations, amenities). Reads are under /public. */
@RestController
@RequestMapping("/api/v1/ukm")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CatalogAdminController {

    private final CatalogService catalogService;

    @PostMapping("/categories")
    public ResponseEntity<GenericResponse<CategoryResponse>> createCategory(
            Authentication authentication, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseUtil.success(
                List.of(catalogService.createCategory(Actor.of(authentication), request)), "Category created"));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<GenericResponse<CategoryResponse>> updateCategory(
            Authentication authentication, @PathVariable Integer id, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(ResponseUtil.success(
                List.of(catalogService.updateCategory(Actor.of(authentication), id, request)), "Category updated"));
    }

    @PostMapping("/locations")
    public ResponseEntity<GenericResponse<LocationResponse>> createLocation(
            Authentication authentication, @Valid @RequestBody LocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseUtil.success(
                List.of(catalogService.createLocation(Actor.of(authentication), request)), "Location created"));
    }

    @PutMapping("/locations/{id}")
    public ResponseEntity<GenericResponse<LocationResponse>> updateLocation(
            Authentication authentication, @PathVariable Integer id, @Valid @RequestBody LocationRequest request) {
        return ResponseEntity.ok(ResponseUtil.success(
                List.of(catalogService.updateLocation(Actor.of(authentication), id, request)), "Location updated"));
    }

    @PostMapping("/amenities")
    public ResponseEntity<GenericResponse<AmenityResponse>> createAmenity(
            Authentication authentication, @Valid @RequestBody AmenityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseUtil.success(
                List.of(catalogService.createAmenity(Actor.of(authentication), request)), "Amenity created"));
    }

    @PutMapping("/amenities/{id}")
    public ResponseEntity<GenericResponse<AmenityResponse>> updateAmenity(
            Authentication authentication, @PathVariable Integer id, @Valid @RequestBody AmenityRequest request) {
        return ResponseEntity.ok(ResponseUtil.success(
                List.of(catalogService.updateAmenity(Actor.of(authentication), id, request)), "Amenity updated"));
    }
}
