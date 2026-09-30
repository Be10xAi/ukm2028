package com.tech10x.ukm.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LocationRequest {

    @NotBlank(message = "city is required")
    @Size(max = 80, message = "city must be at most 80 characters")
    private String city;

    @NotBlank(message = "state is required")
    @Size(max = 80, message = "state must be at most 80 characters")
    private String state;

    @Size(max = 120, message = "areaLandmark must be at most 120 characters")
    private String areaLandmark;

    @DecimalMin(value = "-90.0", message = "latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "latitude must be between -90 and 90")
    @Digits(integer = 2, fraction = 6, message = "latitude allows up to 6 decimal places")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "longitude must be between -180 and 180")
    @Digits(integer = 3, fraction = 6, message = "longitude allows up to 6 decimal places")
    private BigDecimal longitude;
}
