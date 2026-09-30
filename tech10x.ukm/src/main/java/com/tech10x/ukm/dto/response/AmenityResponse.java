package com.tech10x.ukm.dto.response;

import com.tech10x.ukm.entity.Amenity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmenityResponse {
    private Integer id;
    private String name;
    private String icon;

    public static AmenityResponse from(Amenity a) {
        return AmenityResponse.builder().id(a.getId()).name(a.getName()).icon(a.getIcon()).build();
    }
}
