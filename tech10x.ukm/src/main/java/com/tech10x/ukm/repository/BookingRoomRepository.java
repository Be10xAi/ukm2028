package com.tech10x.ukm.repository;

import com.tech10x.ukm.entity.BookingRoom;
import com.tech10x.ukm.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Overlap rule everywhere below: a stay covers the nights [checkInDate, checkOutDate), so it overlaps
 * a requested range [rangeStart, rangeEnd) when checkIn &lt; rangeEnd and checkOut &gt; rangeStart.
 */
@Repository
public interface BookingRoomRepository extends JpaRepository<BookingRoom, Long> {

    @Query("select br from BookingRoom br join fetch br.booking b join fetch br.room r "
            + "where r.id in :roomIds and b.status = :status "
            + "and br.checkInDate < :rangeEnd and br.checkOutDate > :rangeStart")
    List<BookingRoom> findActiveOverlapping(@Param("roomIds") Collection<Long> roomIds,
                                            @Param("status") BookingStatus status,
                                            @Param("rangeStart") LocalDate rangeStart,
                                            @Param("rangeEnd") LocalDate rangeEnd);

    @Query("select br from BookingRoom br join fetch br.booking b join fetch br.room r "
            + "where b.property.id = :propertyId and b.status = :status "
            + "and br.checkInDate < :rangeEnd and br.checkOutDate > :rangeStart")
    List<BookingRoom> findActiveOverlappingByProperty(@Param("propertyId") Long propertyId,
                                                      @Param("status") BookingStatus status,
                                                      @Param("rangeStart") LocalDate rangeStart,
                                                      @Param("rangeEnd") LocalDate rangeEnd);

    @Query("select br from BookingRoom br join br.booking b "
            + "where b.roomType.id = :roomTypeId and b.status = :status "
            + "and br.checkInDate < :rangeEnd and br.checkOutDate > :rangeStart")
    List<BookingRoom> findActiveByRoomTypeOverlapping(@Param("roomTypeId") Long roomTypeId,
                                                      @Param("status") BookingStatus status,
                                                      @Param("rangeStart") LocalDate rangeStart,
                                                      @Param("rangeEnd") LocalDate rangeEnd);

    @Query("select br from BookingRoom br join fetch br.room where br.booking.id in :bookingIds")
    List<BookingRoom> findByBookingIds(@Param("bookingIds") Collection<Long> bookingIds);

    boolean existsByRoom_Id(Long roomDbId);

    @Query("select count(br) from BookingRoom br join br.booking b "
            + "where br.room.id = :roomId and b.status = :status and br.checkOutDate > :today")
    long countActiveUpcomingByRoom(@Param("roomId") Long roomId,
                                   @Param("status") BookingStatus status,
                                   @Param("today") LocalDate today);
}
