package com.tech10x.ukm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * "13. ROOM_INVENTORY" - date-wise room counts per room type (Phase-2 online booking).
 * One row per (room type, date) - enforced by a database unique constraint.
 */
@Entity
@Table(name = "ukm_tbl_room_inventory",
        uniqueConstraints = @UniqueConstraint(name = "uk_inventory_room_type_date",
                columnNames = {"room_type_id", "date"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "roomType")
public class RoomInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_type_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_inventory_room_type"))
    private RoomType roomType;

    @Column(name = "date", nullable = false, updatable = false)
    private LocalDate date;

    @Column(name = "total_rooms", nullable = false)
    private int totalRooms;

    @Column(name = "available_rooms", nullable = false)
    private int availableRooms;

    @Column(name = "price_override", precision = 10, scale = 2)
    private BigDecimal priceOverride;
}
