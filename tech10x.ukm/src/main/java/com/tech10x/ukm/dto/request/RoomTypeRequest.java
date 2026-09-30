package com.tech10x.ukm.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RoomTypeRequest {

    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must be at most 100 characters")
    private String name;

    @NotNull(message = "maxOccupancy is required")
    @Min(value = 1, message = "maxOccupancy must be at least 1")
    @Max(value = 100, message = "maxOccupancy must be at most 100")
    private Integer maxOccupancy;

    // DECIMAL(10,2): at most 8 integer digits and 2 fraction digits, never negative.

    @NotNull(message = "normalTariff is required")
    @DecimalMin(value = "0.00", message = "normalTariff cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "normalTariff allows up to 8 digits and 2 decimals")
    private BigDecimal normalTariff;

    @DecimalMin(value = "0.00", message = "simhasthaTariff cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "simhasthaTariff allows up to 8 digits and 2 decimals")
    private BigDecimal simhasthaTariff;

    @DecimalMin(value = "0.00", message = "peakDateTariff cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "peakDateTariff allows up to 8 digits and 2 decimals")
    private BigDecimal peakDateTariff;
}
