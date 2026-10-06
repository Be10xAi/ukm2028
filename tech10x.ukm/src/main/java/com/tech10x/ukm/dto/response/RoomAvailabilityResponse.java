package com.tech10x.ukm.dto.response;

import com.tech10x.ukm.entity.RoomAvailability;
import com.tech10x.ukm.entity.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Which numbered rooms are booked / free for the stay from {@code from} (check-in) to {@code to}
 * (check-out, exclusive). Owner / admin only - it names guests.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomAvailabilityResponse {

    private LocalDate from;
    private LocalDate to;
    private Summary summary;
    private List<Item> rooms;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private int totalRooms;
        private int booked;
        private int available;
        private int unavailable;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String roomId;
        private String roomNumber;
        private Integer floor;
        private String roomTypeId;
        private String roomTypeName;
        private RoomStatus roomStatus;
        private RoomAvailability availability;
        /** The confirmed stays that overlap the range; empty unless availability is BOOKED. */
        private List<BookingRef> bookings;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingRef {
        private String bookingId;
        private String guestName;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
    }
}
