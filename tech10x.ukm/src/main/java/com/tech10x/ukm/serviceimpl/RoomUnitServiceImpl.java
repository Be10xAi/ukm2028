package com.tech10x.ukm.serviceimpl;

import com.tech10x.ukm.dto.request.RoomUnitBulkRequest;
import com.tech10x.ukm.dto.request.RoomUnitRequest;
import com.tech10x.ukm.dto.request.RoomUnitUpdateRequest;
import com.tech10x.ukm.dto.response.RoomAvailabilityResponse;
import com.tech10x.ukm.dto.response.RoomCalendarResponse;
import com.tech10x.ukm.dto.response.RoomUnitResponse;
import com.tech10x.ukm.entity.AuditAction;
import com.tech10x.ukm.entity.BookingRoom;
import com.tech10x.ukm.entity.Property;
import com.tech10x.ukm.entity.Room;
import com.tech10x.ukm.entity.RoomAvailability;
import com.tech10x.ukm.entity.RoomStatus;
import com.tech10x.ukm.entity.RoomType;
import com.tech10x.ukm.exception.ApiException;
import com.tech10x.ukm.repositoryproxy.BookingProxyRepository;
import com.tech10x.ukm.repositoryproxy.RoomProxyRepository;
import com.tech10x.ukm.repositoryproxy.RoomUnitProxyRepository;
import com.tech10x.ukm.security.Actor;
import com.tech10x.ukm.security.PropertyAccessGuard;
import com.tech10x.ukm.service.AuditService;
import com.tech10x.ukm.service.RoomUnitService;
import com.tech10x.ukm.utils.DateTimeUtil;
import com.tech10x.ukm.utils.RoomNumbers;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomUnitServiceImpl implements RoomUnitService {

    private static final int MAX_ROOMS_PER_PROPERTY = 500;
    private static final int MAX_READ_DAYS = 62;

    private static final Comparator<Room> BY_TYPE_THEN_NUMBER =
            Comparator.comparing((Room r) -> r.getRoomType().getName(), String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Room::getRoomNumber, RoomNumbers.NATURAL);

    private final RoomProxyRepository roomTypes;
    private final RoomUnitProxyRepository units;
    private final BookingProxyRepository bookings;
    private final PropertyAccessGuard guard;
    private final AuditService audit;

    // ================================================================ rooms

    @Override
    @Transactional(readOnly = true)
    public List<RoomUnitResponse> list(Actor actor, String propertyId, String roomTypeId, RoomStatus status) {
        Property property = guard.loadForRead(actor, propertyId);
        List<Room> found = roomTypeId == null
                ? units.findByProperty(property.getId())
                : units.findByPropertyAndType(property.getId(), findRoomType(property, roomTypeId).getId());
        return found.stream()
                .filter(r -> status == null || r.getStatus() == status)
                .sorted(BY_TYPE_THEN_NUMBER)
                .map(RoomUnitResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public RoomUnitResponse create(Actor actor, String propertyId, String roomTypeId, RoomUnitRequest request) {
        Property property = guard.loadForWrite(actor, propertyId);
        RoomType roomType = findRoomType(property, roomTypeId);
        String number = RoomNumbers.normalise(request.getRoomNumber());
        requireCapacity(property, 1);
        requireNumbersFree(property, List.of(number));

        Room room = units.save(Room.builder().property(property).roomType(roomType)
                .roomNumber(number).floor(request.getFloor()).build());
        audit.record(actor.userId(), "ROOM", room.getRoomId(), AuditAction.CREATE, null,
                Map.of("roomNumber", number, "roomTypeId", roomType.getRoomTypeId()));
        return RoomUnitResponse.from(room);
    }

    @Override
    @Transactional
    public List<RoomUnitResponse> createBulk(Actor actor, String propertyId, String roomTypeId,
                                             RoomUnitBulkRequest request) {
        Property property = guard.loadForWrite(actor, propertyId);
        RoomType roomType = findRoomType(property, roomTypeId);

        Set<String> distinct = new LinkedHashSet<>();
        for (String raw : request.getRoomNumbers()) {
            String number = RoomNumbers.normalise(raw);
            if (!distinct.add(number)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Room number " + number + " appears more than once");
            }
        }
        List<String> numbers = new ArrayList<>(distinct);
        requireCapacity(property, numbers.size());
        requireNumbersFree(property, numbers);

        List<Room> created = units.saveAll(numbers.stream()
                .map(n -> Room.builder().property(property).roomType(roomType)
                        .roomNumber(n).floor(request.getFloor()).build())
                .toList());
        audit.record(actor.userId(), "ROOM_TYPE", roomType.getRoomTypeId(), AuditAction.UPDATE, null,
                Map.of("roomsAdded", String.valueOf(numbers.size()),
                        "firstRoom", numbers.get(0), "lastRoom", numbers.get(numbers.size() - 1)));
        return created.stream()
                .sorted(Comparator.comparing(Room::getRoomNumber, RoomNumbers.NATURAL))
                .map(RoomUnitResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public RoomUnitResponse update(Actor actor, String propertyId, String roomId, RoomUnitUpdateRequest request) {
        Property property = guard.loadForWrite(actor, propertyId);
        Room room = findRoom(property, roomId);
        String number = RoomNumbers.normalise(request.getRoomNumber());

        if (!number.equals(room.getRoomNumber())) {
            boolean taken = units.findByNumbers(property.getId(), List.of(number)).stream()
                    .anyMatch(other -> !other.getId().equals(room.getId()));
            if (taken) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "Room number " + number + " already exists in this property");
            }
        }
        // Taking a room out of service while a guest is due to stay in it would leave them with no room.
        if (room.getStatus() == RoomStatus.ACTIVE && request.getStatus() != RoomStatus.ACTIVE
                && bookings.roomHasUpcomingBookings(room.getId(), DateTimeUtil.todayIst())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "This room has upcoming bookings; reassign or cancel them before taking it out of service");
        }

        Map<String, String> before = Map.of("roomNumber", room.getRoomNumber(), "status", room.getStatus().name());
        room.setRoomNumber(number);
        room.setFloor(request.getFloor());
        room.setStatus(request.getStatus());
        units.save(room);
        audit.record(actor.userId(), "ROOM", room.getRoomId(), AuditAction.UPDATE, before,
                Map.of("roomNumber", number, "status", request.getStatus().name()));
        return RoomUnitResponse.from(room);
    }

    @Override
    @Transactional
    public void delete(Actor actor, String propertyId, String roomId) {
        Property property = guard.loadForWrite(actor, propertyId);
        Room room = findRoom(property, roomId);
        // Booking history points at the room; keep it. INACTIVE hides the room from selling instead.
        if (bookings.roomHasBookings(room.getId())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "This room has booking history and cannot be deleted; set its status to INACTIVE instead");
        }
        units.delete(room);
        audit.record(actor.userId(), "ROOM", roomId, AuditAction.DELETE,
                Map.of("roomNumber", room.getRoomNumber()), null);
    }

    // ================================================================ booked / not booked

    @Override
    @Transactional(readOnly = true)
    public RoomAvailabilityResponse availability(Actor actor, String propertyId, String roomTypeId,
                                                 LocalDate from, LocalDate to, RoomAvailability filter) {
        Property property = guard.loadForRead(actor, propertyId);
        LocalDate start = from != null ? from : DateTimeUtil.todayIst();
        LocalDate end = to != null ? to : start.plusDays(1);
        if (!end.isAfter(start)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "to must be after from (to is the check-out date)");
        }
        if (ChronoUnit.DAYS.between(start, end) > MAX_READ_DAYS) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Date range cannot exceed " + MAX_READ_DAYS + " days");
        }

        List<Room> scope = roomTypeId == null
                ? units.findByProperty(property.getId())
                : units.findByPropertyAndType(property.getId(), findRoomType(property, roomTypeId).getId());
        Map<Long, List<BookingRoom>> staysByRoom = bookings
                .findActiveOverlappingByProperty(property.getId(), start, end).stream()
                .collect(Collectors.groupingBy(br -> br.getRoom().getId()));

        List<RoomAvailabilityResponse.Item> all = scope.stream()
                .sorted(BY_TYPE_THEN_NUMBER)
                .map(room -> toItem(room, staysByRoom.getOrDefault(room.getId(), List.of())))
                .toList();

        RoomAvailabilityResponse.Summary summary = RoomAvailabilityResponse.Summary.builder()
                .totalRooms(all.size())
                .booked(count(all, RoomAvailability.BOOKED))
                .available(count(all, RoomAvailability.AVAILABLE))
                .unavailable(count(all, RoomAvailability.UNAVAILABLE))
                .build();
        List<RoomAvailabilityResponse.Item> shown = filter == null ? all
                : all.stream().filter(i -> i.getAvailability() == filter).toList();

        return RoomAvailabilityResponse.builder().from(start).to(end).summary(summary).rooms(shown).build();
    }

    @Override
    @Transactional(readOnly = true)
    public RoomCalendarResponse calendar(Actor actor, String propertyId, String roomId, LocalDate from, LocalDate to) {
        Property property = guard.loadForRead(actor, propertyId);
        Room room = findRoom(property, roomId);
        if (from == null || to == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "from and to dates are required");
        }
        if (to.isBefore(from)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "to must not be before from");
        }
        if (ChronoUnit.DAYS.between(from, to) + 1 > MAX_READ_DAYS) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Date range cannot exceed " + MAX_READ_DAYS + " days");
        }

        List<BookingRoom> stays = bookings.findActiveOverlapping(List.of(room.getId()), from, to.plusDays(1));
        List<RoomCalendarResponse.Day> days = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            final LocalDate night = date;
            Optional<BookingRoom> stay = stays.stream()
                    .filter(br -> !night.isBefore(br.getCheckInDate()) && night.isBefore(br.getCheckOutDate()))
                    .findFirst();
            RoomAvailability availability = stay.isPresent() ? RoomAvailability.BOOKED
                    : room.getStatus() == RoomStatus.ACTIVE ? RoomAvailability.AVAILABLE : RoomAvailability.UNAVAILABLE;
            days.add(RoomCalendarResponse.Day.builder().date(night).availability(availability)
                    .bookingId(stay.map(br -> br.getBooking().getBookingId()).orElse(null))
                    .guestName(stay.map(br -> br.getBooking().getGuestName()).orElse(null))
                    .build());
        }
        return RoomCalendarResponse.builder().roomId(room.getRoomId()).roomNumber(room.getRoomNumber())
                .roomTypeName(room.getRoomType().getName()).roomStatus(room.getStatus()).days(days).build();
    }

    // ================================================================ helpers

    private static RoomAvailabilityResponse.Item toItem(Room room, List<BookingRoom> stays) {
        RoomAvailability availability = !stays.isEmpty() ? RoomAvailability.BOOKED
                : room.getStatus() == RoomStatus.ACTIVE ? RoomAvailability.AVAILABLE : RoomAvailability.UNAVAILABLE;
        List<RoomAvailabilityResponse.BookingRef> refs = stays.stream()
                .sorted(Comparator.comparing(BookingRoom::getCheckInDate))
                .map(br -> RoomAvailabilityResponse.BookingRef.builder()
                        .bookingId(br.getBooking().getBookingId()).guestName(br.getBooking().getGuestName())
                        .checkInDate(br.getCheckInDate()).checkOutDate(br.getCheckOutDate()).build())
                .toList();
        return RoomAvailabilityResponse.Item.builder().roomId(room.getRoomId()).roomNumber(room.getRoomNumber())
                .floor(room.getFloor()).roomTypeId(room.getRoomType().getRoomTypeId())
                .roomTypeName(room.getRoomType().getName()).roomStatus(room.getStatus())
                .availability(availability).bookings(refs).build();
    }

    private static int count(List<RoomAvailabilityResponse.Item> items, RoomAvailability what) {
        return (int) items.stream().filter(i -> i.getAvailability() == what).count();
    }

    /** A room type is only ever looked up THROUGH its property, so ids from another property never resolve. */
    private RoomType findRoomType(Property property, String roomTypeId) {
        return roomTypes.findRoomType(roomTypeId, property.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Room type not found"));
    }

    private Room findRoom(Property property, String roomId) {
        return units.findRoom(roomId, property.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Room not found"));
    }

    private void requireCapacity(Property property, int adding) {
        if (units.countByProperty(property.getId()) + adding > MAX_ROOMS_PER_PROPERTY) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "A property can have at most " + MAX_ROOMS_PER_PROPERTY + " rooms");
        }
    }

    private void requireNumbersFree(Property property, List<String> numbers) {
        List<Room> clash = units.findByNumbers(property.getId(), numbers);
        if (!clash.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Room number " + clash.get(0).getRoomNumber() + " already exists in this property");
        }
    }
}
