package com.tech10x.ukm.service;

import com.tech10x.ukm.dto.request.AmenityRequest;
import com.tech10x.ukm.dto.request.CategoryRequest;
import com.tech10x.ukm.dto.request.LocationRequest;
import com.tech10x.ukm.dto.response.AmenityResponse;
import com.tech10x.ukm.dto.response.CategoryResponse;
import com.tech10x.ukm.dto.response.LocationResponse;
import com.tech10x.ukm.security.Actor;

import java.util.List;

/** Master data: CATEGORY, LOCATION, AMENITY. Readable by anyone; writable by admins only. */
public interface CatalogService {

    List<CategoryResponse> listCategories();

    CategoryResponse createCategory(Actor actor, CategoryRequest request);

    CategoryResponse updateCategory(Actor actor, Integer id, CategoryRequest request);

    List<LocationResponse> listLocations();

    LocationResponse createLocation(Actor actor, LocationRequest request);

    LocationResponse updateLocation(Actor actor, Integer id, LocationRequest request);

    List<AmenityResponse> listAmenities();

    AmenityResponse createAmenity(Actor actor, AmenityRequest request);

    AmenityResponse updateAmenity(Actor actor, Integer id, AmenityRequest request);
}
