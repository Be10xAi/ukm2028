package com.tech10x.ukm.serviceimpl;

import com.tech10x.ukm.dto.request.AmenityRequest;
import com.tech10x.ukm.dto.request.CategoryRequest;
import com.tech10x.ukm.dto.request.LocationRequest;
import com.tech10x.ukm.dto.response.AmenityResponse;
import com.tech10x.ukm.dto.response.CategoryResponse;
import com.tech10x.ukm.dto.response.LocationResponse;
import com.tech10x.ukm.entity.Amenity;
import com.tech10x.ukm.entity.AuditAction;
import com.tech10x.ukm.entity.Category;
import com.tech10x.ukm.entity.Location;
import com.tech10x.ukm.exception.ApiException;
import com.tech10x.ukm.repositoryproxy.CatalogProxyRepository;
import com.tech10x.ukm.security.Actor;
import com.tech10x.ukm.service.AuditService;
import com.tech10x.ukm.service.CatalogService;
import com.tech10x.ukm.utils.TextSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final CatalogProxyRepository catalog;
    private final AuditService audit;

    // ---------------------------------------------------------------- categories

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return catalog.findAllCategories().stream().map(CategoryResponse::from).toList();
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(Actor actor, CategoryRequest request) {
        requireAdmin(actor);
        Category category = new Category();
        applyCategory(category, request, null);
        category = catalog.saveCategory(category);
        audit.record(actor.userId(), "CATEGORY", String.valueOf(category.getId()), AuditAction.CREATE,
                null, Map.of("name", category.getName(), "slug", category.getSlug()));
        return CategoryResponse.from(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Actor actor, Integer id, CategoryRequest request) {
        requireAdmin(actor);
        Category category = catalog.findCategory(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found"));
        Map<String, String> before = Map.of("name", category.getName(), "slug", category.getSlug());
        applyCategory(category, request, id);
        category = catalog.saveCategory(category);
        audit.record(actor.userId(), "CATEGORY", String.valueOf(id), AuditAction.UPDATE, before,
                Map.of("name", category.getName(), "slug", category.getSlug()));
        return CategoryResponse.from(category);
    }

    private void applyCategory(Category category, CategoryRequest request, Integer excludeId) {
        String name = TextSanitizer.plain(request.getName());
        if (name == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "name is required");
        }
        if (catalog.categoryNameTaken(name, excludeId)) {
            throw new ApiException(HttpStatus.CONFLICT, "A category with this name already exists");
        }
        if (catalog.categorySlugTaken(request.getSlug(), excludeId)) {
            throw new ApiException(HttpStatus.CONFLICT, "A category with this slug already exists");
        }
        category.setName(name);
        category.setSlug(request.getSlug());
        category.setDescription(TextSanitizer.plain(request.getDescription()));
    }

    // ---------------------------------------------------------------- locations

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> listLocations() {
        return catalog.findAllLocations().stream().map(LocationResponse::from).toList();
    }

    @Override
    @Transactional
    public LocationResponse createLocation(Actor actor, LocationRequest request) {
        requireAdmin(actor);
        Location location = new Location();
        applyLocation(location, request);
        location = catalog.saveLocation(location);
        audit.record(actor.userId(), "LOCATION", String.valueOf(location.getId()), AuditAction.CREATE,
                null, Map.of("city", location.getCity(), "state", location.getState()));
        return LocationResponse.from(location);
    }

    @Override
    @Transactional
    public LocationResponse updateLocation(Actor actor, Integer id, LocationRequest request) {
        requireAdmin(actor);
        Location location = catalog.findLocation(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Location not found"));
        Map<String, String> before = Map.of("city", location.getCity(), "state", location.getState());
        applyLocation(location, request);
        location = catalog.saveLocation(location);
        audit.record(actor.userId(), "LOCATION", String.valueOf(id), AuditAction.UPDATE, before,
                Map.of("city", location.getCity(), "state", location.getState()));
        return LocationResponse.from(location);
    }

    private void applyLocation(Location location, LocationRequest request) {
        String city = TextSanitizer.plain(request.getCity());
        String state = TextSanitizer.plain(request.getState());
        if (city == null || state == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "city and state are required");
        }
        location.setCity(city);
        location.setState(state);
        location.setAreaLandmark(TextSanitizer.plain(request.getAreaLandmark()));
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
    }

    // ---------------------------------------------------------------- amenities

    @Override
    @Transactional(readOnly = true)
    public List<AmenityResponse> listAmenities() {
        return catalog.findAllAmenities().stream().map(AmenityResponse::from).toList();
    }

    @Override
    @Transactional
    public AmenityResponse createAmenity(Actor actor, AmenityRequest request) {
        requireAdmin(actor);
        Amenity amenity = new Amenity();
        applyAmenity(amenity, request, null);
        amenity = catalog.saveAmenity(amenity);
        audit.record(actor.userId(), "AMENITY", String.valueOf(amenity.getId()), AuditAction.CREATE,
                null, Map.of("name", amenity.getName()));
        return AmenityResponse.from(amenity);
    }

    @Override
    @Transactional
    public AmenityResponse updateAmenity(Actor actor, Integer id, AmenityRequest request) {
        requireAdmin(actor);
        Amenity amenity = catalog.findAmenity(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Amenity not found"));
        Map<String, String> before = Map.of("name", amenity.getName());
        applyAmenity(amenity, request, id);
        amenity = catalog.saveAmenity(amenity);
        audit.record(actor.userId(), "AMENITY", String.valueOf(id), AuditAction.UPDATE, before,
                Map.of("name", amenity.getName()));
        return AmenityResponse.from(amenity);
    }

    private void applyAmenity(Amenity amenity, AmenityRequest request, Integer excludeId) {
        String name = TextSanitizer.plain(request.getName());
        if (name == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "name is required");
        }
        if (catalog.amenityNameTaken(name, excludeId)) {
            throw new ApiException(HttpStatus.CONFLICT, "An amenity with this name already exists");
        }
        amenity.setName(name);
        amenity.setIcon(request.getIcon() == null || request.getIcon().isBlank() ? null : request.getIcon());
    }

    /** Defence in depth: the controller already has @PreAuthorize, but the service never trusts that alone. */
    private static void requireAdmin(Actor actor) {
        if (!actor.admin()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }
}
