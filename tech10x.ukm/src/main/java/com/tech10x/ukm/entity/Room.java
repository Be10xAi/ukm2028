package com.tech10x.ukm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/**
 * "ROOM" - one physical, numbered room (e.g. "101") that belongs to a room type of a property.
 * ROOM_TYPE is what is sold; ROOM is what the guest is actually given.
 * The room number is unique within a property.
 */
@Entity
@Table(name = "ukm_tbl_rooms",
        uniqueConstraints = @UniqueConstraint(name = "uk_room_property_number",
                columnNames = {"property_id", "room_number"}),
        indexes = @Index(name = "idx_room_room_type", columnList = "room_type_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"property", "roomType"})
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false, unique = true, length = 40, updatable = false)
    private String roomId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_room_property"))
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_type_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_room_room_type"))
    private RoomType roomType;

    /** Stored upper-case and trimmed, e.g. "101" or "A-12". */
    @Column(name = "room_number", nullable = false, length = 20)
    private String roomNumber;

    @Column(name = "floor_no")
    private Integer floor;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status = RoomStatus.ACTIVE;

    @PrePersist
    void onCreate() {
        if (this.roomId == null) {
            this.roomId = "RM" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        }
    }
}
