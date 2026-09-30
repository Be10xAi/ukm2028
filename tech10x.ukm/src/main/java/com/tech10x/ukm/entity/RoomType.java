package com.tech10x.ukm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/** "12. ROOM_TYPE" - a sellable room category within a property (e.g. 'Deluxe AC Room'). */
@Entity
@Table(name = "ukm_tbl_room_types",
        uniqueConstraints = @UniqueConstraint(name = "uk_room_type_property_name",
                columnNames = {"property_id", "name"}),
        indexes = @Index(name = "idx_room_type_property", columnList = "property_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "property")
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_type_id", nullable = false, unique = true, length = 40, updatable = false)
    private String roomTypeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_room_type_property"))
    private Property property;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "max_occupancy", nullable = false)
    private int maxOccupancy;

    /** Money is always DECIMAL(10,2), never FLOAT. */
    @Column(name = "normal_tariff", nullable = false, precision = 10, scale = 2)
    private BigDecimal normalTariff;

    @Column(name = "simhastha_tariff", precision = 10, scale = 2)
    private BigDecimal simhasthaTariff;

    @Column(name = "peak_date_tariff", precision = 10, scale = 2)
    private BigDecimal peakDateTariff;

    @PrePersist
    void onCreate() {
        if (this.roomTypeId == null) {
            this.roomTypeId = "RMT" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        }
    }
}
