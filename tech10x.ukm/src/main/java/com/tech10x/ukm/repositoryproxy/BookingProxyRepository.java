package com.tech10x.ukm.repositoryproxy;

import com.tech10x.ukm.entity.Booking;
import com.tech10x.ukm.entity.BookingRoom;
import com.tech10x.ukm.entity.BookingStatus;
import com.tech10x.ukm.repository.BookingRepository;
import com.tech10x.ukm.repository.BookingRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** BOOKING and BOOKING_ROOM. Only CONFIRMED bookings hold rooms. */
@Component
@RequiredArgsConstructor
public class BookingProxyRepository {

    private final BookingRepository bookingRepository;
    private final BookingRoomRepository bookingRoomRepository;

    // ---- booking ----
    public Optional<Booking> findBooking(String bookingId) {
        return bookingRepository.findByBookingId(bookingId);
    }

    public Optional<Booking> findBookingForUpdate(String bookingId) {
        return bookingRepository.findByBookingIdForUpdate(bookingId);
    }

    public Page<Booking> findByGuest(String guestUserId, Pageable pageable) {
        return bookingRepository.findByGuestUserId(guestUserId, pageable);
    }

    public Page<Booking> searchByProperty(Long propertyDbId, Collection<BookingStatus> statuses,
                                          LocalDate rangeStart, LocalDate rangeEnd, Pageable pageable) {
        return bookingRepository.searchByProperty(propertyDbId, statuses, rangeStart, rangeEnd, pageable);
    }

    public Booking saveBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    // ---- booking rooms ----
    public List<BookingRoom> findBookingRooms(Collection<Long> bookingDbIds) {
        return bookingDbIds.isEmpty() ? List.of() : bookingRoomRepository.findByBookingIds(bookingDbIds);
    }

    public List<BookingRoom> saveAllBookingRooms(List<BookingRoom> rows) {
        return bookingRoomRepository.saveAll(rows);
    }

    public void deleteBookingRooms(List<BookingRoom> rows) {
        bookingRoomRepository.deleteAll(rows);
    }

    /** Confirmed stays that hold any of these rooms during [rangeStart, rangeEnd). */
    public List<BookingRoom> findActiveOverlapping(Collection<Long> roomDbIds, LocalDate rangeStart, LocalDate rangeEnd) {
        return roomDbIds.isEmpty() ? List.of()
                : bookingRoomRepository.findActiveOverlapping(roomDbIds, BookingStatus.CONFIRMED, rangeStart, rangeEnd);
    }

    public List<BookingRoom> findActiveOverlappingByProperty(Long propertyDbId, LocalDate rangeStart, LocalDate rangeEnd) {
        return bookingRoomRepository.findActiveOverlappingByProperty(
                propertyDbId, BookingStatus.CONFIRMED, rangeStart, rangeEnd);
    }

    public List<BookingRoom> findActiveByRoomTypeOverlapping(Long roomTypeDbId, LocalDate rangeStart, LocalDate rangeEnd) {
        return bookingRoomRepository.findActiveByRoomTypeOverlapping(
                roomTypeDbId, BookingStatus.CONFIRMED, rangeStart, rangeEnd);
    }

    public boolean roomHasBookings(Long roomDbId) {
        return bookingRoomRepository.existsByRoom_Id(roomDbId);
    }

    public boolean roomHasUpcomingBookings(Long roomDbId, LocalDate today) {
        return bookingRoomRepository.countActiveUpcomingByRoom(roomDbId, BookingStatus.CONFIRMED, today) > 0;
    }
}
