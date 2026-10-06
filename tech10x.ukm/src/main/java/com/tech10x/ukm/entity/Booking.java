package com.tech10x.ukm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * "BOOKING" - a stay of one or more rooms of ONE room type. The stay covers the nights from
 * {@code checkInDate} up to (but not including) {@code checkOutDate}, so a room is free for a new
 * guest to check in on the day the previous guest checks out.
 * The actual numbered rooms are in BOOKING_ROOM.
 */
@Entity
@Table(name = "ukm_tbl_bookings",
        indexes = {
                @Index(name = "idx_booking_property_dates", columnList = "property_id, check_in_date, check_out_date"),
                @Index(name = "idx_booking_guest", columnList = "guest_user_id"),
                @Index(name = "idx_booking_room_type", columnList = "room_type_id")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"property", "roomType"})
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false, unique = true, length = 40, updatable = false)
    private String bookingId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_booking_property"))
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_type_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_booking_room_type"))
    private RoomType roomType;

    /** userId of the registered guest who booked online; null for a walk-in entered by the owner. */
    @Column(name = "guest_user_id", length = 40, updatable = false)
    private String guestUserId;

    @Column(name = "guest_name", nullable = false, length = 100)
    private String guestName;

    @Column(name = "guest_phone", nullable = false, length = 15)
    private String guestPhone;

    @Column(name = "check_in_date", nullable = false, updatable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false, updatable = false)
    private LocalDate checkOutDate;

    @Column(name = "rooms_count", nullable = false, updatable = false)
    private int roomsCount;

    @Column(name = "guest_count", nullable = false)
    private int guestCount;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2, updatable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "created_by", length = 40, updatable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by", length = 40)
    private String cancelledBy;

    @PrePersist
    void onCreate() {
        if (this.bookingId == null) {
            this.bookingId = "BKG" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
