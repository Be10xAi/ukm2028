package com.tech10x.ukm.serviceimpl;

import com.tech10x.ukm.dto.request.RoomInventoryRequest;
import com.tech10x.ukm.dto.request.RoomTypeRequest;
import com.tech10x.ukm.dto.response.PublicInventoryResponse;
import com.tech10x.ukm.dto.response.RoomInventoryResponse;
import com.tech10x.ukm.dto.response.RoomTypeResponse;
import com.tech10x.ukm.entity.Property;
import com.tech10x.ukm.entity.PropertyStatus;
import com.tech10x.ukm.entity.RoomInventory;
import com.tech10x.ukm.entity.RoomType;
import com.tech10x.ukm.exception.ApiException;
import com.tech10x.ukm.repositoryproxy.PropertyProxyRepository;
import com.tech10x.ukm.repositoryproxy.RoomProxyRepository;
import com.tech10x.ukm.security.Actor;
import com.tech10x.ukm.security.PropertyAccessGuard;
import com.tech10x.ukm.service.RoomService;
import com.tech10x.ukm.utils.DateTimeUtil;
import com.tech10x.ukm.utils.TextSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private static final int MAX_ROOM_TYPES_PER_PROPERTY = 50;
    /** Longest range a supplier can write in one call / the public can read in one call. */
    private static final int MAX_WRITE_DAYS = 366;
    private static final int MAX_READ_DAYS = 62;

    private final RoomProxyRepository rooms;
    private final PropertyProxyRepository properties;
    private final PropertyAccessGuard guard;

    // ================================================================ room types

    @Override
    @Transactional(readOnly = true)
    public List<RoomTypeResponse> listRoomTypes(Actor actor, String propertyId) {
        Property property = guard.loadForRead(actor, propertyId);
        return rooms.findRoomTypes(property.getId()).stream().map(RoomTypeResponse::from).toList();
    }

    @Override
    @Transactional
    public RoomTypeResponse createRoomType(Actor actor, String propertyId, RoomTypeRequest request) {
        Property property = guard.loadForWrite(actor, propertyId);
        if (rooms.countRoomTypes(property.getId()) >= MAX_ROOM_TYPES_PER_PROPERTY) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "A property can have at most " + MAX_ROOM_TYPES_PER_PROPERTY + " room types");
        }
        RoomType roomType = RoomType.builder().property(property).build();
        apply(roomType, request, null);
        return RoomTypeResponse.from(rooms.saveRoomType(roomType));
    }

    @Override
    @Transactional
    public RoomTypeResponse updateRoomType(Actor actor, String propertyId, String roomTypeId,
                                           RoomTypeRequest request) {
        Property property = guard.loadForWrite(actor, propertyId);
        RoomType roomType = findRoomType(property, roomTypeId);
        apply(roomType, request, roomType.getId());
        return RoomTypeResponse.from(rooms.saveRoomType(roomType));
    }

    @Override
    @Transactional
    public void deleteRoomType(Actor actor, String propertyId, String roomTypeId) {
        Property property = guard.loadForWrite(actor, propertyId);
        RoomType roomType = findRoomType(property, roomTypeId);
        // Inventory (and later bookings) reference the room type; don't destroy that history.
        if (rooms.hasInventory(roomType.getId())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "This room type already has inventory and cannot be deleted");
        }
        rooms.deleteRoomType(roomType);
    }

    // ================================================================ inventory

    @Override
    @Transactional
    public List<RoomInventoryResponse> upsertInventory(Actor actor, String propertyId, String roomTypeId,
                                                       RoomInventoryRequest request) {
        Property property = guard.loadForWrite(actor, propertyId);
        RoomType roomType = findRoomType(property, roomTypeId);

        LocalDate from = request.getFromDate();
        LocalDate to = request.getToDate();
        if (from.isBefore(DateTimeUtil.todayIst())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "fromDate cannot be in the past");
        }
        requireRange(from, to, MAX_WRITE_DAYS);
        if (request.getAvailableRooms() > request.getTotalRooms()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "availableRooms cannot exceed totalRooms");
        }

        // NOTE for the booking module: this endpoint SETS counts. When bookings exist, seat
        // reservation must use an atomic conditional decrement (or a row lock) on available_rooms,
        // not read-modify-write, and a supplier edit must not push available below what is booked.
        Map<LocalDate, RoomInventory> existing = rooms.findInventory(roomType.getId(), from, to).stream()
                .collect(Collectors.toMap(RoomInventory::getDate, Function.identity()));

        List<RoomInventory> batch = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            RoomInventory row = existing.get(d);
            if (row == null) {
                row = RoomInventory.builder().roomType(roomType).date(d).build();
            }
            row.setTotalRooms(request.getTotalRooms());
            row.setAvailableRooms(request.getAvailableRooms());
            row.setPriceOverride(request.getPriceOverride());
            batch.add(row);
        }
        return rooms.saveAllInventory(batch).stream().map(RoomInventoryResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomInventoryResponse> getInventory(Actor actor, String propertyId, String roomTypeId,
                                                    LocalDate from, LocalDate to) {
        Property property = guard.loadForRead(actor, propertyId);
        RoomType roomType = findRoomType(property, roomTypeId);
        requireRange(from, to, MAX_READ_DAYS);
        return rooms.findInventory(roomType.getId(), from, to).stream().map(RoomInventoryResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicInventoryResponse> getPublicInventory(String propertyId, String roomTypeId,
                                                            LocalDate from, LocalDate to) {
        Property property = properties.findByPropertyId(propertyId)
                .filter(p -> p.getStatus() == PropertyStatus.LIVE)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Property not found"));
        RoomType roomType = findRoomType(property, roomTypeId);
        requireRange(from, to, MAX_READ_DAYS);
        return rooms.findInventory(roomType.getId(), from, to).stream().map(PublicInventoryResponse::from).toList();
    }

    // ================================================================ helpers

    /** A room type is only ever looked up THROUGH its property, so ids from another property never resolve. */
    private RoomType findRoomType(Property property, String roomTypeId) {
        return rooms.findRoomType(roomTypeId, property.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Room type not found"));
    }

    private void apply(RoomType roomType, RoomTypeRequest request, Long excludeId) {
        String name = TextSanitizer.plain(request.getName());
        if (name == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "name is required");
        }
        if (rooms.roomTypeNameTaken(roomType.getProperty().getId(), name, excludeId)) {
            throw new ApiException(HttpStatus.CONFLICT, "This property already has a room type with that name");
        }
        roomType.setName(name);
        roomType.setMaxOccupancy(request.getMaxOccupancy());
        roomType.setNormalTariff(request.getNormalTariff());
        roomType.setSimhasthaTariff(request.getSimhasthaTariff());
        roomType.setPeakDateTariff(request.getPeakDateTariff());
    }

    private static void requireRange(LocalDate from, LocalDate to, int maxDays) {
        if (from == null || to == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "from and to dates are required");
        }
        if (to.isBefore(from)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "toDate must not be before fromDate");
        }
        if (ChronoUnit.DAYS.between(from, to) + 1 > maxDays) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Date range cannot exceed " + maxDays + " days");
        }
    }
}
