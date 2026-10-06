package com.tech10x.ukm.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * A guest booking a LIVE property. Rooms are assigned automatically; the guest never picks a room
 * number. checkOutDate is the day the guest leaves (that night is not charged).
 */
@Getter
@Setter
public class BookingRequest {

    @NotBlank(message = "propertyId is required")
    @Size(max = 40, message = "propertyId is too long")
    private String propertyId;

    @NotBlank(message = "roomTypeId is required")
    @Size(max = 40, message = "roomTypeId is too long")
    private String roomTypeId;

    @NotNull(message = "checkInDate is required")
    private LocalDate checkInDate;

    @NotNull(message = "checkOutDate is required")
    private LocalDate checkOutDate;

    @NotNull(message = "roomsCount is required")
    @Min(value = 1, message = "roomsCount must be at least 1")
    @Max(value = 50, message = "roomsCount must be at most 50")
    private Integer roomsCount;

    @NotNull(message = "guestCount is required")
    @Min(value = 1, message = "guestCount must be at least 1")
    @Max(value = 1000, message = "guestCount is too large")
    private Integer guestCount;

    /** Optional - defaults to the logged-in user's name. */
    @Size(max = 100, message = "guestName must be at most 100 characters")
    private String guestName;

    /** Optional - defaults to the logged-in user's mobile number. */
    @Pattern(regexp = "^[0-9]{10,15}$", message = "guestPhone must be 10-15 digits")
    private String guestPhone;
}
