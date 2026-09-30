package com.tech10x.ukm.dto.response;

import com.tech10x.ukm.entity.PropertyStatus;
import com.tech10x.ukm.entity.PropertyVerifiedStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Compact card used in search results and in "my properties" / admin lists. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertySummaryResponse {
    private String propertyId;
    private String name;
    private String categoryName;
    private String city;
    private String areaLandmark;
    private String coverImageUrl;
    /** Lowest normal tariff across the property's room types; null if it has none yet. */
    private BigDecimal startingPrice;
    private PropertyVerifiedStatus verifiedStatus;
    private PropertyStatus status;
}
