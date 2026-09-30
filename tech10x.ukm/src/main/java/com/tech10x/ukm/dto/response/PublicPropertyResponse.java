package com.tech10x.ukm.dto.response;

import com.tech10x.ukm.entity.PropertyVerifiedStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * What an anonymous visitor may see of a LIVE property. No supplier identity, no workflow
 * status, no documents, no timestamps - a separate class so nothing internal can leak by accident.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicPropertyResponse {
    private String propertyId;
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
    private List<PropertyImageResponse> images;
    private List<AmenityResponse> amenities;
    private List<RoomTypeResponse> roomTypes;
}
