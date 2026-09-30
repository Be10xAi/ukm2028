package com.tech10x.ukm.dto.response;

import com.tech10x.ukm.entity.PropertyStatus;
import com.tech10x.ukm.entity.PropertyVerifiedStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Full owner / admin view, including workflow status. Public pages use {@link PublicPropertyResponse}. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyResponse {
    private String propertyId;
    private String supplierId;
    private CategoryResponse category;
    private LocationResponse location;
    private String name;
    private String description;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private String cancellationPolicy;
    private PropertyVerifiedStatus verifiedStatus;
    private LocalDate lastVerifiedDate;
    private PropertyStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PropertyImageResponse> images;
    private List<AmenityResponse> amenities;
}
