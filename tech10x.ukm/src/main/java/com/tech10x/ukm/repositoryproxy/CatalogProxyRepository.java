package com.tech10x.ukm.repositoryproxy;

import com.tech10x.ukm.entity.Amenity;
import com.tech10x.ukm.entity.Category;
import com.tech10x.ukm.entity.Location;
import com.tech10x.ukm.repository.AmenityRepository;
import com.tech10x.ukm.repository.CategoryRepository;
import com.tech10x.ukm.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Master data: CATEGORY, LOCATION, AMENITY. */
@Component
@RequiredArgsConstructor
public class CatalogProxyRepository {

    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final AmenityRepository amenityRepository;

    // ---- category ----
    public List<Category> findAllCategories() {
        return categoryRepository.findAll(Sort.by("name"));
    }

    public Optional<Category> findCategory(Integer id) {
        return categoryRepository.findById(id);
    }

    public boolean categoryNameTaken(String name, Integer excludeId) {
        return excludeId == null
                ? categoryRepository.existsByNameIgnoreCase(name)
                : categoryRepository.existsByNameIgnoreCaseAndIdNot(name, excludeId);
    }

    public boolean categorySlugTaken(String slug, Integer excludeId) {
        return excludeId == null
                ? categoryRepository.existsBySlug(slug)
                : categoryRepository.existsBySlugAndIdNot(slug, excludeId);
    }

    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    // ---- location ----
    public List<Location> findAllLocations() {
        return locationRepository.findAll(Sort.by("city", "areaLandmark"));
    }

    public Optional<Location> findLocation(Integer id) {
        return locationRepository.findById(id);
    }

    public Location saveLocation(Location location) {
        return locationRepository.save(location);
    }

    // ---- amenity ----
    public List<Amenity> findAllAmenities() {
        return amenityRepository.findAll(Sort.by("name"));
    }

    public List<Amenity> findAmenitiesByIds(Collection<Integer> ids) {
        return amenityRepository.findAllById(ids);
    }

    public Optional<Amenity> findAmenity(Integer id) {
        return amenityRepository.findById(id);
    }

    public boolean amenityNameTaken(String name, Integer excludeId) {
        return excludeId == null
                ? amenityRepository.existsByNameIgnoreCase(name)
                : amenityRepository.existsByNameIgnoreCaseAndIdNot(name, excludeId);
    }

    public Amenity saveAmenity(Amenity amenity) {
        return amenityRepository.save(amenity);
    }
}
