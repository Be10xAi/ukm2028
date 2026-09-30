package com.tech10x.ukm.dto.response;

import com.tech10x.ukm.entity.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeResponse {
    private String roomTypeId;
    private String name;
    private int maxOccupancy;
    private BigDecimal normalTariff;
    private BigDecimal simhasthaTariff;
    private BigDecimal peakDateTariff;

    public static RoomTypeResponse from(RoomType r) {
        return RoomTypeResponse.builder().roomTypeId(r.getRoomTypeId()).name(r.getName())
                .maxOccupancy(r.getMaxOccupancy()).normalTariff(r.getNormalTariff())
                .simhasthaTariff(r.getSimhasthaTariff()).peakDateTariff(r.getPeakDateTariff()).build();
    }
}
