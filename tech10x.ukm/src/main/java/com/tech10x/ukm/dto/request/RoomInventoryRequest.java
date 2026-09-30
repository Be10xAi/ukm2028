package com.tech10x.ukm.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Sets the same room counts for every date from {@code fromDate} to {@code toDate} inclusive. */
@Getter
@Setter
public class RoomInventoryRequest {

    @NotNull(message = "fromDate is required")
    private LocalDate fromDate;

    @NotNull(message = "toDate is required")
    private LocalDate toDate;

    @NotNull(message = "totalRooms is required")
    @Min(value = 0, message = "totalRooms cannot be negative")
    @Max(value = 10000, message = "totalRooms is too large")
    private Integer totalRooms;

    @NotNull(message = "availableRooms is required")
    @Min(value = 0, message = "availableRooms cannot be negative")
    @Max(value = 10000, message = "availableRooms is too large")
    private Integer availableRooms;

    /** Optional per-date price; null clears any existing override. */
    @DecimalMin(value = "0.00", message = "priceOverride cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "priceOverride allows up to 8 digits and 2 decimals")
    private BigDecimal priceOverride;
}
