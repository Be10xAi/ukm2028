package com.tech10x.ukm.dto.response;

import com.tech10x.ukm.entity.RoomInventory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Owner / admin view of one inventory day (includes the supplier's total room count). */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomInventoryResponse {
    private LocalDate date;
    private int totalRooms;
    private int availableRooms;
    private BigDecimal priceOverride;

    public static RoomInventoryResponse from(RoomInventory i) {
        return RoomInventoryResponse.builder().date(i.getDate()).totalRooms(i.getTotalRooms())
                .availableRooms(i.getAvailableRooms()).priceOverride(i.getPriceOverride()).build();
    }
}
