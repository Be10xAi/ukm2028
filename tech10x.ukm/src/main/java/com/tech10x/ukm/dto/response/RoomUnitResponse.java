package com.tech10x.ukm.dto.response;

import com.tech10x.ukm.entity.Room;
import com.tech10x.ukm.entity.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomUnitResponse {
    private String roomId;
    private String roomNumber;
    private Integer floor;
    private String roomTypeId;
    private String roomTypeName;
    private RoomStatus status;

    public static RoomUnitResponse from(Room r) {
        return RoomUnitResponse.builder().roomId(r.getRoomId()).roomNumber(r.getRoomNumber())
                .floor(r.getFloor()).roomTypeId(r.getRoomType().getRoomTypeId())
                .roomTypeName(r.getRoomType().getName()).status(r.getStatus()).build();
    }
}
