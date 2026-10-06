package com.tech10x.ukm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * "BOOKING_ROOM" - links a booking to one numbered room. The dates are copied from the booking so
 * "which rooms are taken between X and Y" is a single indexed query on this table.
 * A room is taken for a night when a row of a CONFIRMED booking covers it.
 */
@Entity
@Table(name = "ukm_tbl_booking_rooms",
        uniqueConstraints = @UniqueConstraint(name = "uk_booking_room", columnNames = {"booking_id", "room_id"}),
        indexes = @Index(name = "idx_booking_room_room_dates", columnList = "room_id, check_in_date, check_out_date"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"booking", "room"})
public class BookingRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_booking_room_booking"))
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_booking_room_room"))
    private Room room;

    @Column(name = "check_in_date", nullable = false, updatable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false, updatable = false)
    private LocalDate checkOutDate;
}
