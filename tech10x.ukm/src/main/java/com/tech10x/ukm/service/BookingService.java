package com.tech10x.ukm.service;

import com.tech10x.ukm.dto.request.BookingRequest;
import com.tech10x.ukm.dto.request.ManualBookingRequest;
import com.tech10x.ukm.dto.request.RoomAssignmentRequest;
import com.tech10x.ukm.dto.response.BookingResponse;
import com.tech10x.ukm.entity.BookingStatus;
import com.tech10x.ukm.security.Actor;

import java.time.LocalDate;
import java.util.List;

/** BOOKING and BOOKING_ROOM - holds numbered rooms and keeps the date-wise inventory counts in step. */
public interface BookingService {

    /** A logged-in guest books a LIVE property; free rooms are assigned automatically. */
    BookingResponse createBooking(Actor actor, BookingRequest request);

    /** The owner / admin enters a walk-in or phone booking, optionally choosing the room numbers. */
    BookingResponse createManualBooking(Actor actor, String propertyId, ManualBookingRequest request);

    BookingResponse get(Actor actor, String bookingId);

    List<BookingResponse> listMine(Actor actor, int page, int size);

    List<BookingResponse> listForProperty(Actor actor, String propertyId, BookingStatus status,
                                          LocalDate from, LocalDate to, int page, int size);

    BookingResponse cancel(Actor actor, String bookingId);

    /** Owner / admin changes which numbered rooms a confirmed booking holds. */
    BookingResponse reassignRooms(Actor actor, String bookingId, RoomAssignmentRequest request);
}
