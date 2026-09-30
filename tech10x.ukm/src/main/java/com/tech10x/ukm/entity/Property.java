package com.tech10x.ukm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * "7. PROPERTY" - the core listing entity, one row per hotel/dharamshala/camp/homestay.
 * <p>
 * Never hard-deleted (per the design doc): use {@link PropertyStatus#SUSPENDED} instead.
 * {@code propertyId} is an unguessable public identifier used in every URL, so the
 * sequential database {@code id} is never exposed.
 */
@Entity
@Table(name = "ukm_tbl_properties",
        indexes = {
                @Index(name = "idx_property_supplier", columnList = "supplier_id"),
                @Index(name = "idx_property_status", columnList = "status"),
                @Index(name = "idx_property_category", columnList = "category_id"),
                @Index(name = "idx_property_location", columnList = "location_id")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"supplier", "category", "location", "amenities"})
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Public, unguessable identifier (PRP + 16 hex chars). */
    @Column(name = "property_id", nullable = false, unique = true, length = 40, updatable = false)
    private String propertyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_property_supplier"))
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_property_category"))
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_property_location"))
    private Location location;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "check_in_time")
    private LocalTime checkInTime;

    @Column(name = "check_out_time")
    private LocalTime checkOutTime;

    @Column(name = "cancellation_policy", columnDefinition = "TEXT")
    private String cancellationPolicy;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "verified_status", nullable = false, length = 20)
    private PropertyVerifiedStatus verifiedStatus = PropertyVerifiedStatus.UNVERIFIED;

    @Column(name = "last_verified_date")
    private LocalDate lastVerifiedDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PropertyStatus status = PropertyStatus.DRAFT;

    /** "11. PROPERTY_AMENITY" - pure join table, composite PK (property_id + amenity_id). */
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "ukm_tbl_property_amenities",
            joinColumns = @JoinColumn(name = "property_id",
                    foreignKey = @ForeignKey(name = "fk_pa_property")),
            inverseJoinColumns = @JoinColumn(name = "amenity_id",
                    foreignKey = @ForeignKey(name = "fk_pa_amenity")))
    private Set<Amenity> amenities = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.propertyId == null) {
            this.propertyId = "PRP" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
