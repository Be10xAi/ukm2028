package com.tech10x.ukm.security;

import com.tech10x.ukm.entity.Property;
import com.tech10x.ukm.entity.PropertyStatus;
import com.tech10x.ukm.entity.Supplier;
import com.tech10x.ukm.exception.ApiException;
import com.tech10x.ukm.repositoryproxy.PropertyProxyRepository;
import com.tech10x.ukm.repositoryproxy.SupplierProxyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * The single place that decides "may this caller touch this property?" (object-level
 * authorization, the defence against IDOR). Every property/room/inventory/image/document
 * operation goes through here, so a supplier can never read or change another supplier's data
 * by guessing an id.
 * <p>
 * Non-owners get the same 404 as a missing property, so ids can't be probed for existence.
 */
@Component
@RequiredArgsConstructor
public class PropertyAccessGuard {

    private final PropertyProxyRepository properties;
    private final SupplierProxyRepository suppliers;

    /** Owner or admin may read (including DRAFT / SUSPENDED properties). */
    public Property loadForRead(Actor actor, String propertyId) {
        Property property = properties.findByPropertyId(propertyId)
                .orElseThrow(PropertyAccessGuard::notFound);
        if (actor.admin()) {
            return property;
        }
        Supplier supplier = suppliers.findByUserId(actor.userId())
                .orElseThrow(PropertyAccessGuard::notFound);
        if (!property.getSupplier().getId().equals(supplier.getId())) {
            throw notFound();
        }
        return property;
    }

    /** Like {@link #loadForRead}, but a supplier can no longer edit once an admin suspended the property. */
    public Property loadForWrite(Actor actor, String propertyId) {
        Property property = loadForRead(actor, propertyId);
        if (!actor.admin() && property.getStatus() == PropertyStatus.SUSPENDED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This property is suspended; please contact support");
        }
        return property;
    }

    private static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "Property not found");
    }
}
