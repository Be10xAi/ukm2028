package com.tech10x.ukm.dto.response;

import com.tech10x.ukm.entity.RoomAvailability;
import com.tech10x.ukm.entity.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/** Night-by-night view of one room. A date is the NIGHT starting that day (check-out day is free). */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomCalendarResponse {

    private String roomId;
    private String roomNumber;
    private String roomTypeName;
    private RoomStatus roomStatus;
    private List<Day> days;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Day {
        private LocalDate date;
        private RoomAvailability availability;
        private String bookingId;
        private String guestName;
    }
}
