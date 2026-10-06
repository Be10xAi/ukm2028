package com.tech10x.ukm.serviceimpl;

import com.tech10x.ukm.dto.request.BookingRequest;
import com.tech10x.ukm.dto.request.ManualBookingRequest;
import com.tech10x.ukm.dto.request.RoomAssignmentRequest;
import com.tech10x.ukm.dto.response.BookingResponse;
import com.tech10x.ukm.entity.AuditAction;
import com.tech10x.ukm.entity.Booking;
import com.tech10x.ukm.entity.BookingRoom;
import com.tech10x.ukm.entity.BookingStatus;
import com.tech10x.ukm.entity.Property;
import com.tech10x.ukm.entity.PropertyStatus;
import com.tech10x.ukm.entity.Room;
import com.tech10x.ukm.entity.RoomInventory;
import com.tech10x.ukm.entity.RoomType;
import com.tech10x.ukm.entity.Users;
import com.tech10x.ukm.exception.ApiException;
import com.tech10x.ukm.repositoryproxy.BookingProxyRepository;
import com.tech10x.ukm.repositoryproxy.PropertyProxyRepository;
import com.tech10x.ukm.repositoryproxy.RoomProxyRepository;
import com.tech10x.ukm.repositoryproxy.RoomUnitProxyRepository;
import com.tech10x.ukm.repositoryproxy.SupplierProxyRepository;
import com.tech10x.ukm.repositoryproxy.UserRepository;
import com.tech10x.ukm.security.Actor;
import com.tech10x.ukm.security.PropertyAccessGuard;
import com.tech10x.ukm.service.AuditService;
import com.tech10x.ukm.service.BookingService;
import com.tech10x.ukm.utils.DateTimeUtil;
import com.tech10x.ukm.utils.RoomNumbers;
import com.tech10x.ukm.utils.TextSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * How double booking is prevented: every operation that takes or gives back rooms of a room type
 * first locks that room type's inventory rows for the affected nights (see
 * RoomInventoryRepository#readByRoomType_IdAndDateBetweenOrderByDateAsc). Two bookings that could ever want the same room share at
 * least one night, so they queue on that lock and the second one sees the first one's rooms as taken.
 */
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final int MAX_NIGHTS = 30;
    private static final int MAX_PAGE_SIZE = 50;
    /** Stand-ins for "no limit" so the search query never needs a null parameter. */
    private static final LocalDate MIN_DATE = LocalDate.of(2000, 1, 1);
    private static final LocalDate MAX_DATE = LocalDate.of(2100, 1, 1);

    private final BookingProxyRepository bookings;
    private final RoomProxyRepository roomTypes;
    private final RoomUnitProxyRepository units;
    private final PropertyProxyRepository properties;
    private final SupplierProxyRepository suppliers;
    private final UserRepository users;
    private final PropertyAccessGuard guard;
    private final AuditService audit;

    /** A loaded booking plus whether the caller acts for the property (owner / admin) or only as the guest. */
    private record BookingAccess(Booking booking, boolean staff) {
    }

    // ================================================================ create

    @Override
    @Transactional
    public BookingResponse createBooking(Actor actor, BookingRequest request) {
        Property property = properties.findByPropertyId(request.getPropertyId())
                .filter(p -> p.getStatus() == PropertyStatus.LIVE)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Property not found"));
        Users guest = users.findByUserId(actor.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        String name = TextSanitizer.plain(request.getGuestName());
        if (name == null) {
            name = guest.getName();
        }
        String phone = request.getGuestPhone() != null ? request.getGuestPhone() : guest.getMobileNo();
        if (phone == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "guestPhone is required");
        }
        return book(actor, property, request.getRoomTypeId(), request.getCheckInDate(),
                request.getCheckOutDate(), request.getRoomsCount(), request.getGuestCount(),
                name, phone, actor.userId(), null);
    }

    @Override
    @Transactional
    public BookingResponse createManualBooking(Actor actor, String propertyId, ManualBookingRequest request) {
        Property property = guard.loadForWrite(actor, propertyId);

        String name = TextSanitizer.plain(request.getGuestName());
        if (name == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "guestName is required");
        }
        List<String> numbers = request.getRoomNumbers() == null ? List.of() : distinctNumbers(request.getRoomNumbers());
        int roomsCount;
        if (!numbers.isEmpty()) {
            if (request.getRoomsCount() != null && request.getRoomsCount() != numbers.size()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "roomsCount does not match the number of roomNumbers");
            }
            roomsCount = numbers.size();
        } else if (request.getRoomsCount() != null) {
            roomsCount = request.getRoomsCount();
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "roomNumbers or roomsCount is required");
        }
        return book(actor, property, request.getRoomTypeId(), request.getCheckInDate(),
                request.getCheckOutDate(), roomsCount, request.getGuestCount(),
                name, request.getGuestPhone(), null, numbers);
    }

    /** The single place that creates a booking. {@code requestedNumbers} empty/null = auto-assign. */
    private BookingResponse book(Actor actor, Property property, String roomTypeId,
                                 LocalDate checkIn, LocalDate checkOut, int roomsCount, int guestCount,
                                 String guestName, String guestPhone, String guestUserId,
                                 List<String> requestedNumbers) {
        LocalDate today = DateTimeUtil.todayIst();
        if (checkIn.isBefore(today)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "checkInDate cannot be in the past");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "checkOutDate must be after checkInDate");
        }
        if (ChronoUnit.DAYS.between(checkIn, checkOut) > MAX_NIGHTS) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A booking cannot be longer than " + MAX_NIGHTS + " nights");
        }
        RoomType roomType = roomTypes.findRoomType(roomTypeId, property.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Room type not found"));
        if (guestCount > roomsCount * roomType.getMaxOccupancy()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "At most " + (roomsCount * roomType.getMaxOccupancy())
                    + " guests fit in " + roomsCount + " room(s) of this type");
        }

        // 1. Lock the date-wise counts, then check every night has enough rooms and add up the price.
        LocalDate lastNight = checkOut.minusDays(1);
        List<RoomInventory> inventory = roomTypes.findInventoryForUpdate(roomType.getId(), checkIn, lastNight);
        Map<LocalDate, RoomInventory> byDate = inventory.stream()
                .collect(Collectors.toMap(RoomInventory::getDate, Function.identity()));
        BigDecimal total = BigDecimal.ZERO;
        for (LocalDate night = checkIn; !night.isAfter(lastNight); night = night.plusDays(1)) {
            RoomInventory row = byDate.get(night);
            if (row == null) {
                throw new ApiException(HttpStatus.CONFLICT, "Rooms are not open for booking on " + night);
            }
            if (row.getAvailableRooms() < roomsCount) {
                throw new ApiException(HttpStatus.CONFLICT, row.getAvailableRooms() == 0
                        ? "No rooms available on " + night
                        : "Only " + row.getAvailableRooms() + " room(s) left on " + night);
            }
            BigDecimal nightly = row.getPriceOverride() != null ? row.getPriceOverride() : roomType.getNormalTariff();
            total = total.add(nightly.multiply(BigDecimal.valueOf(roomsCount)));
        }

        // 2. Pick the numbered rooms that are free for the whole stay.
        List<Room> active = units.findActiveByType(roomType.getId());
        if (active.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "No rooms have been set up for this room type yet");
        }
        Set<Long> busy = busyRoomIds(active, checkIn, checkOut, null);
        List<Room> chosen = requestedNumbers == null || requestedNumbers.isEmpty()
                ? autoAssign(active, busy, roomsCount)
                : pickRequested(active, busy, requestedNumbers);

        // 3. Save the booking, link its rooms, and take the rooms out of the date-wise counts.
        Booking booking = bookings.saveBooking(Booking.builder()
                .property(property).roomType(roomType).guestUserId(guestUserId)
                .guestName(guestName).guestPhone(guestPhone)
                .checkInDate(checkIn).checkOutDate(checkOut)
                .roomsCount(roomsCount).guestCount(guestCount)
                .totalAmount(total.setScale(2, RoundingMode.HALF_UP))
                .status(BookingStatus.CONFIRMED).createdBy(actor.userId()).build());
        bookings.saveAllBookingRooms(chosen.stream()
                .map(room -> BookingRoom.builder().booking(booking).room(room)
                        .checkInDate(checkIn).checkOutDate(checkOut).build())
                .toList());
        for (RoomInventory row : inventory) {
            row.setAvailableRooms(row.getAvailableRooms() - roomsCount);
        }
        roomTypes.saveAllInventory(inventory);

        List<String> numbers = numbersOf(chosen);
        audit.record(actor.userId(), "BOOKING", booking.getBookingId(), AuditAction.CREATE, null,
                Map.of("propertyId", property.getPropertyId(), "roomTypeId", roomType.getRoomTypeId(),
                        "checkIn", checkIn.toString(), "checkOut", checkOut.toString(),
                        "rooms", String.join(",", numbers), "status", BookingStatus.CONFIRMED.name()));
        return BookingResponse.from(booking, numbers);
    }

    // ================================================================ read

    @Override
    @Transactional(readOnly = true)
    public BookingResponse get(Actor actor, String bookingId) {
        return toResponse(access(actor, bookingId, false).booking());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> listMine(Actor actor, int page, int size) {
        return toResponses(bookings.findByGuest(actor.userId(), pageable(page, size)).getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> listForProperty(Actor actor, String propertyId, BookingStatus status,
                                                 LocalDate from, LocalDate to, int page, int size) {
        Property property = guard.loadForRead(actor, propertyId);
        if (from != null && to != null && to.isBefore(from)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "to must not be before from");
        }
        Collection<BookingStatus> statuses = status == null ? List.of(BookingStatus.values()) : List.of(status);
        LocalDate rangeStart = from == null ? MIN_DATE : from;
        LocalDate rangeEnd = to == null ? MAX_DATE : to.plusDays(1);
        return toResponses(bookings.searchByProperty(property.getId(), statuses, rangeStart, rangeEnd,
                pageable(page, size)).getContent());
    }

    // ================================================================ cancel

    @Override
    @Transactional
    public BookingResponse cancel(Actor actor, String bookingId) {
        BookingAccess access = access(actor, bookingId, true);
        Booking booking = access.booking();
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ApiException(HttpStatus.CONFLICT, "Only a confirmed booking can be cancelled");
        }
        LocalDate today = DateTimeUtil.todayIst();
        if (!booking.getCheckOutDate().isAfter(today)) {
            throw new ApiException(HttpStatus.CONFLICT, "This stay is already over");
        }
        if (!access.staff() && booking.getCheckInDate().isBefore(today)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "This stay has already started; please contact the property to cancel");
        }

        List<RoomInventory> inventory = roomTypes.findInventoryForUpdate(booking.getRoomType().getId(),
                booking.getCheckInDate(), booking.getCheckOutDate().minusDays(1));
        for (RoomInventory row : inventory) {
            row.setAvailableRooms(Math.min(row.getTotalRooms(), row.getAvailableRooms() + booking.getRoomsCount()));
        }
        roomTypes.saveAllInventory(inventory);

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancelledBy(actor.userId());
        bookings.saveBooking(booking);
        audit.record(actor.userId(), "BOOKING", booking.getBookingId(), AuditAction.STATUS_CHANGE,
                Map.of("status", BookingStatus.CONFIRMED.name()), Map.of("status", BookingStatus.CANCELLED.name()));
        return toResponse(booking);
    }

    // ================================================================ reassign rooms

    @Override
    @Transactional
    public BookingResponse reassignRooms(Actor actor, String bookingId, RoomAssignmentRequest request) {
        BookingAccess access = access(actor, bookingId, true);
        if (!access.staff()) {
            throw bookingNotFound();
        }
        Booking booking = access.booking();
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ApiException(HttpStatus.CONFLICT, "Only a confirmed booking can be changed");
        }
        if (!booking.getCheckOutDate().isAfter(DateTimeUtil.todayIst())) {
            throw new ApiException(HttpStatus.CONFLICT, "This stay is already over");
        }
        List<String> numbers = distinctNumbers(request.getRoomNumbers());
        if (numbers.size() != booking.getRoomsCount()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Exactly " + booking.getRoomsCount() + " room number(s) are required for this booking");
        }

        // Same lock as creating a booking, so nobody can grab one of these rooms while we move.
        roomTypes.findInventoryForUpdate(booking.getRoomType().getId(),
                booking.getCheckInDate(), booking.getCheckOutDate().minusDays(1));
        List<Room> active = units.findActiveByType(booking.getRoomType().getId());
        Set<Long> busy = busyRoomIds(active, booking.getCheckInDate(), booking.getCheckOutDate(), booking.getId());
        List<Room> chosen = pickRequested(active, busy, numbers);

        List<BookingRoom> current = bookings.findBookingRooms(List.of(booking.getId()));
        Set<Long> currentIds = current.stream().map(br -> br.getRoom().getId()).collect(Collectors.toSet());
        Set<Long> chosenIds = chosen.stream().map(Room::getId).collect(Collectors.toSet());
        List<BookingRoom> toRemove = current.stream().filter(br -> !chosenIds.contains(br.getRoom().getId())).toList();
        List<BookingRoom> toAdd = chosen.stream()
                .filter(room -> !currentIds.contains(room.getId()))
                .map(room -> BookingRoom.builder().booking(booking).room(room)
                        .checkInDate(booking.getCheckInDate()).checkOutDate(booking.getCheckOutDate()).build())
                .toList();
        // Only rooms that really change are deleted / inserted, so the (booking, room) unique key never clashes.
        bookings.deleteBookingRooms(toRemove);
        bookings.saveAllBookingRooms(toAdd);

        List<String> before = current.stream().map(br -> br.getRoom().getRoomNumber())
                .sorted(RoomNumbers.NATURAL).toList();
        List<String> after = numbersOf(chosen);
        audit.record(actor.userId(), "BOOKING", booking.getBookingId(), AuditAction.UPDATE,
                Map.of("rooms", String.join(",", before)), Map.of("rooms", String.join(",", after)));
        return BookingResponse.from(booking, after);
    }

    // ================================================================ helpers

    private List<Room> autoAssign(List<Room> active, Set<Long> busy, int roomsCount) {
        List<Room> free = active.stream()
                .filter(room -> !busy.contains(room.getId()))
                .sorted(Comparator.comparing(Room::getRoomNumber, RoomNumbers.NATURAL))
                .toList();
        if (free.size() < roomsCount) {
            // Counts said there was space but the numbered rooms disagree (e.g. fewer rooms were created
            // than the inventory total) - refuse rather than oversell.
            throw new ApiException(HttpStatus.CONFLICT,
                    "Only " + free.size() + " numbered room(s) are free for these dates");
        }
        return new ArrayList<>(free.subList(0, roomsCount));
    }

    private List<Room> pickRequested(List<Room> active, Set<Long> busy, List<String> numbers) {
        Map<String, Room> byNumber = active.stream().collect(Collectors.toMap(Room::getRoomNumber, Function.identity()));
        List<Room> chosen = new ArrayList<>();
        for (String number : numbers) {
            Room room = byNumber.get(number);
            if (room == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Room " + number + " is not an active room of this room type");
            }
            if (busy.contains(room.getId())) {
                throw new ApiException(HttpStatus.CONFLICT, "Room " + number + " is already booked for these dates");
            }
            chosen.add(room);
        }
        return chosen;
    }

    /** Ids of rooms held by another confirmed stay during [from, to). */
    private Set<Long> busyRoomIds(Collection<Room> rooms, LocalDate from, LocalDate to, Long excludeBookingId) {
        List<Long> ids = rooms.stream().map(Room::getId).toList();
        return bookings.findActiveOverlapping(ids, from, to).stream()
                .filter(br -> excludeBookingId == null || !br.getBooking().getId().equals(excludeBookingId))
                .map(br -> br.getRoom().getId())
                .collect(Collectors.toSet());
    }

    private static List<String> distinctNumbers(List<String> raw) {
        Set<String> distinct = new LinkedHashSet<>();
        for (String value : raw) {
            String number = RoomNumbers.normalise(value);
            if (!distinct.add(number)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Room number " + number + " appears more than once");
            }
        }
        return new ArrayList<>(distinct);
    }

    private static List<String> numbersOf(List<Room> rooms) {
        return rooms.stream().map(Room::getRoomNumber).sorted(RoomNumbers.NATURAL).toList();
    }

    /** Guests see only their own bookings, owners only their property's, admins all; anyone else gets a 404. */
    private BookingAccess access(Actor actor, String bookingId, boolean forUpdate) {
        Booking booking = (forUpdate ? bookings.findBookingForUpdate(bookingId) : bookings.findBooking(bookingId))
                .orElseThrow(BookingServiceImpl::bookingNotFound);
        if (actor.admin() || isPropertyOwner(actor, booking)) {
            return new BookingAccess(booking, true);
        }
        if (actor.userId().equals(booking.getGuestUserId())) {
            return new BookingAccess(booking, false);
        }
        throw bookingNotFound();
    }

    private boolean isPropertyOwner(Actor actor, Booking booking) {
        return suppliers.findByUserId(actor.userId())
                .map(s -> s.getId().equals(booking.getProperty().getSupplier().getId()))
                .orElse(false);
    }

    private BookingResponse toResponse(Booking booking) {
        return toResponses(List.of(booking)).get(0);
    }

    /** One query for the room numbers of the whole list instead of one per booking. */
    private List<BookingResponse> toResponses(List<Booking> list) {
        Map<Long, List<String>> numbersByBooking = bookings
                .findBookingRooms(list.stream().map(Booking::getId).toList()).stream()
                .collect(Collectors.groupingBy(br -> br.getBooking().getId(),
                        Collectors.mapping(br -> br.getRoom().getRoomNumber(), Collectors.toList())));
        return list.stream()
                .map(b -> BookingResponse.from(b, numbersByBooking.getOrDefault(b.getId(), List.of()).stream()
                        .sorted(RoomNumbers.NATURAL).toList()))
                .toList();
    }

    private static Pageable pageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
    }

    private static ApiException bookingNotFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "Booking not found");
    }
}
