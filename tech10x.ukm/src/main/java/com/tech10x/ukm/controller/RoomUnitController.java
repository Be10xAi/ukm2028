package com.tech10x.ukm.controller;

import com.tech10x.ukm.dto.request.RoomUnitBulkRequest;
import com.tech10x.ukm.dto.request.RoomUnitRequest;
import com.tech10x.ukm.dto.request.RoomUnitUpdateRequest;
import com.tech10x.ukm.dto.response.GenericResponse;
import com.tech10x.ukm.dto.response.RoomAvailabilityResponse;
import com.tech10x.ukm.dto.response.RoomCalendarResponse;
import com.tech10x.ukm.dto.response.RoomUnitResponse;
import com.tech10x.ukm.entity.RoomAvailability;
import com.tech10x.ukm.entity.RoomStatus;
import com.tech10x.ukm.security.Actor;
import com.tech10x.ukm.service.RoomUnitService;
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

/** The numbered rooms of a property, and which of them are booked - owner supplier or admin only. */
@RestController
@RequestMapping("/api/v1/ukm/properties/{propertyId}")
@RequiredArgsConstructor
public class RoomUnitController {

    private final RoomUnitService roomUnitService;

    @PostMapping("/room-types/{roomTypeId}/rooms")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<RoomUnitResponse>> create(
            Authentication authentication, @PathVariable String propertyId, @PathVariable String roomTypeId,
            @Valid @RequestBody RoomUnitRequest request) {
        RoomUnitResponse created = roomUnitService.create(Actor.of(authentication), propertyId, roomTypeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success(List.of(created), "Room created"));
    }

    @PostMapping("/room-types/{roomTypeId}/rooms/bulk")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<RoomUnitResponse>> createBulk(
            Authentication authentication, @PathVariable String propertyId, @PathVariable String roomTypeId,
            @Valid @RequestBody RoomUnitBulkRequest request) {
        List<RoomUnitResponse> created =
                roomUnitService.createBulk(Actor.of(authentication), propertyId, roomTypeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success(created, created.size() + " rooms created"));
    }

    /** All rooms of the property; optional ?roomTypeId= and ?status=ACTIVE|MAINTENANCE|INACTIVE. */
    @GetMapping("/rooms")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<RoomUnitResponse>> list(
            Authentication authentication, @PathVariable String propertyId,
            @RequestParam(required = false) String roomTypeId,
            @RequestParam(required = false) RoomStatus status) {
        return ResponseEntity.ok(ResponseUtil.success(
                roomUnitService.list(Actor.of(authentication), propertyId, roomTypeId, status)));
    }

    @PutMapping("/rooms/{roomId}")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<RoomUnitResponse>> update(
            Authentication authentication, @PathVariable String propertyId, @PathVariable String roomId,
            @Valid @RequestBody RoomUnitUpdateRequest request) {
        return ResponseEntity.ok(ResponseUtil.success(
                List.of(roomUnitService.update(Actor.of(authentication), propertyId, roomId, request)),
                "Room updated"));
    }

    @DeleteMapping("/rooms/{roomId}")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<Object>> delete(
            Authentication authentication, @PathVariable String propertyId, @PathVariable String roomId) {
        roomUnitService.delete(Actor.of(authentication), propertyId, roomId);
        return ResponseEntity.ok(ResponseUtil.success("Room deleted"));
    }

    /**
     * Which numbered rooms are booked / available / unavailable for the stay ?from=yyyy-MM-dd (check-in)
     * to ?to=yyyy-MM-dd (check-out, exclusive). With no dates it answers for tonight.
     * Optional ?roomTypeId= and ?status=BOOKED|AVAILABLE|UNAVAILABLE narrow the list.
     */
    @GetMapping("/rooms/availability")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<RoomAvailabilityResponse>> availability(
            Authentication authentication, @PathVariable String propertyId,
            @RequestParam(required = false) String roomTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) RoomAvailability status) {
        return ResponseEntity.ok(ResponseUtil.success(List.of(
                roomUnitService.availability(Actor.of(authentication), propertyId, roomTypeId, from, to, status))));
    }

    /** One room night by night for ?from= to ?to= inclusive, max 62 days. */
    @GetMapping("/rooms/{roomId}/calendar")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<RoomCalendarResponse>> calendar(
            Authentication authentication, @PathVariable String propertyId, @PathVariable String roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ResponseUtil.success(List.of(
                roomUnitService.calendar(Actor.of(authentication), propertyId, roomId, from, to))));
    }
}
