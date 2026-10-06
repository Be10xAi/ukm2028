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
import java.util.List;

/**
 * A booking entered by the property owner or an admin (walk-in or phone booking). Unlike a guest
 * booking it may name the exact rooms. Give {@code roomNumbers} to choose rooms, or only
 * {@code roomsCount} to let the system pick free ones.
 */
@Getter
@Setter
public class ManualBookingRequest {

    @NotBlank(message = "roomTypeId is required")
    @Size(max = 40, message = "roomTypeId is too long")
    private String roomTypeId;

    @NotNull(message = "checkInDate is required")
    private LocalDate checkInDate;

    @NotNull(message = "checkOutDate is required")
    private LocalDate checkOutDate;

    @Min(value = 1, message = "roomsCount must be at least 1")
    @Max(value = 50, message = "roomsCount must be at most 50")
    private Integer roomsCount;

    @Size(max = 50, message = "at most 50 rooms per booking")
    private List<@NotBlank(message = "room numbers cannot be blank")
            @Size(max = 20, message = "roomNumber must be at most 20 characters") String> roomNumbers;

    @NotNull(message = "guestCount is required")
    @Min(value = 1, message = "guestCount must be at least 1")
    @Max(value = 1000, message = "guestCount is too large")
    private Integer guestCount;

    @NotBlank(message = "guestName is required")
    @Size(max = 100, message = "guestName must be at most 100 characters")
    private String guestName;

    @NotBlank(message = "guestPhone is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "guestPhone must be 10-15 digits")
    private String guestPhone;
}
