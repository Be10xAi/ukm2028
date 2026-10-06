package com.tech10x.ukm.repository;

import com.tech10x.ukm.entity.Booking;
import com.tech10x.ukm.entity.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingId(String bookingId);

    /** Row lock, so two concurrent cancels / reassignments of one booking cannot both act on it. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.bookingId = :bookingId")
    Optional<Booking> findByBookingIdForUpdate(@Param("bookingId") String bookingId);

    Page<Booking> findByGuestUserId(String guestUserId, Pageable pageable);

    /** Stays of a property that overlap [rangeStart, rangeEnd) - the caller always passes concrete values. */
    @Query("select b from Booking b where b.property.id = :propertyId "
            + "and b.status in :statuses "
            + "and b.checkOutDate > :rangeStart and b.checkInDate < :rangeEnd")
    Page<Booking> searchByProperty(@Param("propertyId") Long propertyId,
                                   @Param("statuses") Collection<BookingStatus> statuses,
                                   @Param("rangeStart") LocalDate rangeStart,
                                   @Param("rangeEnd") LocalDate rangeEnd,
                                   Pageable pageable);
}
