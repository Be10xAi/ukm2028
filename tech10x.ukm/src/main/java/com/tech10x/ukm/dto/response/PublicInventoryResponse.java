package com.tech10x.ukm.dto.response;

import com.tech10x.ukm.entity.RoomInventory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Public availability view - deliberately omits totalRooms (business-sensitive). */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicInventoryResponse {
    private LocalDate date;
    private int availableRooms;
    private BigDecimal priceOverride;

    public static PublicInventoryResponse from(RoomInventory i) {
        return PublicInventoryResponse.builder().date(i.getDate())
                .availableRooms(i.getAvailableRooms()).priceOverride(i.getPriceOverride()).build();
    }
}
