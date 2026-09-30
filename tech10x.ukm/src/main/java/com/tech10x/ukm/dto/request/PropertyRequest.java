package com.tech10x.ukm.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Create / update payload for a PROPERTY. Deliberately has NO supplier, status, verifiedStatus
 * or lastVerifiedDate field: the owner comes from the JWT and the status fields are admin-only,
 * so a supplier cannot set them by adding extra JSON keys (mass assignment).
 */
@Getter
@Setter
public class PropertyRequest {

    @NotNull(message = "categoryId is required")
    private Integer categoryId;

    @NotNull(message = "locationId is required")
    private Integer locationId;

    @NotBlank(message = "name is required")
    @Size(max = 150, message = "name must be at most 150 characters")
    private String name;

    @Size(max = 5000, message = "description must be at most 5000 characters")
    private String description;

    @NotBlank(message = "address is required")
    @Size(max = 500, message = "address must be at most 500 characters")
    private String address;

    @DecimalMin(value = "-90.0", message = "latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "latitude must be between -90 and 90")
    @Digits(integer = 2, fraction = 6, message = "latitude allows up to 6 decimal places")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "longitude must be between -180 and 180")
    @Digits(integer = 3, fraction = 6, message = "longitude allows up to 6 decimal places")
    private BigDecimal longitude;

    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    @Size(max = 5000, message = "cancellationPolicy must be at most 5000 characters")
    private String cancellationPolicy;
}
