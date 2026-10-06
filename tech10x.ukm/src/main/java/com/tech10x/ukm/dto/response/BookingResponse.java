package com.tech10x.ukm.dto.response;

import com.tech10x.ukm.entity.Booking;
import com.tech10x.ukm.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private String bookingId;
    private BookingStatus status;
    private String propertyId;
    private String propertyName;
    private String roomTypeId;
    private String roomTypeName;
    private String guestName;
    private String guestPhone;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private long nights;
    private int roomsCount;
    private int guestCount;
    private BigDecimal totalAmount;
    /** The numbered rooms this booking holds, in natural order. */
    private List<String> roomNumbers;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;

    public static BookingResponse from(Booking b, List<String> roomNumbers) {
        return BookingResponse.builder().bookingId(b.getBookingId()).status(b.getStatus())
                .propertyId(b.getProperty().getPropertyId()).propertyName(b.getProperty().getName())
                .roomTypeId(b.getRoomType().getRoomTypeId()).roomTypeName(b.getRoomType().getName())
                .guestName(b.getGuestName()).guestPhone(b.getGuestPhone())
                .checkInDate(b.getCheckInDate()).checkOutDate(b.getCheckOutDate())
                .nights(ChronoUnit.DAYS.between(b.getCheckInDate(), b.getCheckOutDate()))
                .roomsCount(b.getRoomsCount()).guestCount(b.getGuestCount())
                .totalAmount(b.getTotalAmount()).roomNumbers(roomNumbers)
                .createdAt(b.getCreatedAt()).cancelledAt(b.getCancelledAt()).build();
    }
}
