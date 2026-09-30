package com.tech10x.ukm.controller;

import com.tech10x.ukm.dto.request.RoomInventoryRequest;
import com.tech10x.ukm.dto.request.RoomTypeRequest;
import com.tech10x.ukm.dto.response.GenericResponse;
import com.tech10x.ukm.dto.response.RoomInventoryResponse;
import com.tech10x.ukm.dto.response.RoomTypeResponse;
import com.tech10x.ukm.security.Actor;
import com.tech10x.ukm.service.RoomService;
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

/** Room types and date-wise inventory of a property - owner supplier or admin only. */
@RestController
@RequestMapping("/api/v1/ukm/properties/{propertyId}/room-types")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<RoomTypeResponse>> list(
            Authentication authentication, @PathVariable String propertyId) {
        return ResponseEntity.ok(ResponseUtil.success(
                roomService.listRoomTypes(Actor.of(authentication), propertyId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<RoomTypeResponse>> create(
            Authentication authentication, @PathVariable String propertyId,
            @Valid @RequestBody RoomTypeRequest request) {
        RoomTypeResponse created = roomService.createRoomType(Actor.of(authentication), propertyId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success(List.of(created), "Room type created"));
    }

    @PutMapping("/{roomTypeId}")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<RoomTypeResponse>> update(
            Authentication authentication, @PathVariable String propertyId, @PathVariable String roomTypeId,
            @Valid @RequestBody RoomTypeRequest request) {
        return ResponseEntity.ok(ResponseUtil.success(
                List.of(roomService.updateRoomType(Actor.of(authentication), propertyId, roomTypeId, request)),
                "Room type updated"));
    }

    @DeleteMapping("/{roomTypeId}")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<Object>> delete(
            Authentication authentication, @PathVariable String propertyId, @PathVariable String roomTypeId) {
        roomService.deleteRoomType(Actor.of(authentication), propertyId, roomTypeId);
        return ResponseEntity.ok(ResponseUtil.success("Room type deleted"));
    }

    /** Set room counts (and optional price override) for a date range, max 366 days per call. */
    @PutMapping("/{roomTypeId}/inventory")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<RoomInventoryResponse>> upsertInventory(
            Authentication authentication, @PathVariable String propertyId, @PathVariable String roomTypeId,
            @Valid @RequestBody RoomInventoryRequest request) {
        return ResponseEntity.ok(ResponseUtil.success(
                roomService.upsertInventory(Actor.of(authentication), propertyId, roomTypeId, request),
                "Inventory updated"));
    }

    /** Read inventory for ?from=yyyy-MM-dd&to=yyyy-MM-dd, max 62 days per request. */
    @GetMapping("/{roomTypeId}/inventory")
    @PreAuthorize("hasAnyRole('SUPPLIER','ADMIN')")
    public ResponseEntity<GenericResponse<RoomInventoryResponse>> getInventory(
            Authentication authentication, @PathVariable String propertyId, @PathVariable String roomTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ResponseUtil.success(
                roomService.getInventory(Actor.of(authentication), propertyId, roomTypeId, from, to)));
    }
}
