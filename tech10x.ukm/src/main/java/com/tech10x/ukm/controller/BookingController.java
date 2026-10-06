package com.tech10x.ukm.controller;

import com.tech10x.ukm.dto.request.BookingRequest;
import com.tech10x.ukm.dto.request.ManualBookingRequest;
import com.tech10x.ukm.dto.request.RoomAssignmentRequest;
import com.tech10x.ukm.dto.response.BookingResponse;
import com.tech10x.ukm.dto.response.GenericResponse;
import com.tech10x.ukm.entity.BookingStatus;
import com.tech10x.ukm.security.Actor;
import com.tech10x.ukm.service.BookingService;
import com.tech10x.ukm.utils.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Room bookings. Guests use /bookings; owners and admins also use the property-scoped endpoints. */
@RestController
@RequestMapping("/api/v1/ukm")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // ---------------------------------------------------------------- guest (any logged-in user)

    @PostMapping("/bookings")
    public ResponseEntity<GenericResponse<BookingResponse>> create(
            Authentication authentication, @Valid @RequestBody BookingRequest request) {
        BookingResponse created = bookingService.createBooking(Actor.of(authentication), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success(List.of(created), "Booking confirmed"));
    }

    @GetMapping("/bookings/mine")
    public ResponseEntity<GenericResponse<BookingResponse>> mine(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ResponseUtil.success(
                bookingService.listMine(Actor.of(authentication), page, size)));
    }

    /** The guest who booked, the property's owner supplier, or an admin. */
    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<GenericResponse<BookingResponse>> get(
            Authentication authentication, @PathVariable String bookingId) {
        return ResponseEntity.ok(ResponseUtil.success(
                List.of(bookingService.get(Actor.of(authentication), bookingId))));
    }

    @PatchMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<GenericResponse<BookingResponse>> cancel(
            Authentication authentication, @PathVariable String bookingId) {
        return ResponseEntity.ok(ResponseUtil.success(
                List.of(bookingService.cancel(Actor.of(authentication), bookingId)), "Booking cancelled"));
    }

    // ---------------------------------------------------------------- owner supplier / admin

    /** Walk-in or phone booking; may name the exact room numbers. */
    @PostMapping("/properties/{propertyId}/bookings")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<BookingResponse>> createManual(
            Authentication authentication, @PathVariable String propertyId,
            @Valid @RequestBody ManualBookingRequest request) {
        BookingResponse created = bookingService.createManualBooking(Actor.of(authentication), propertyId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success(List.of(created), "Booking confirmed"));
    }

    /** Bookings of a property; optional ?status=CONFIRMED|CANCELLED and stays overlapping ?from= to ?to=. */
    @GetMapping("/properties/{propertyId}/bookings")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<BookingResponse>> listForProperty(
            Authentication authentication, @PathVariable String propertyId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ResponseUtil.success(bookingService.listForProperty(
                Actor.of(authentication), propertyId, status, from, to, page, size)));
    }

    /** Change which numbered rooms a confirmed booking holds. */
    @PatchMapping("/bookings/{bookingId}/rooms")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<BookingResponse>> reassignRooms(
            Authentication authentication, @PathVariable String bookingId,
            @Valid @RequestBody RoomAssignmentRequest request) {
        return ResponseEntity.ok(ResponseUtil.success(
                List.of(bookingService.reassignRooms(Actor.of(authentication), bookingId, request)),
                "Rooms updated"));
    }
}
