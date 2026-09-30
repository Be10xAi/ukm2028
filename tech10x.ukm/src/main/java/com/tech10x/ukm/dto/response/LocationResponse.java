package com.tech10x.ukm.dto.response;

import com.tech10x.ukm.entity.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponse {
    private Integer id;
    private String city;
    private String state;
    private String areaLandmark;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public static LocationResponse from(Location l) {
        return LocationResponse.builder().id(l.getId()).city(l.getCity()).state(l.getState())
                .areaLandmark(l.getAreaLandmark()).latitude(l.getLatitude()).longitude(l.getLongitude()).build();
    }
}
