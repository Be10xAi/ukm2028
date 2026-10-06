package com.tech10x.ukm.service;

import com.tech10x.ukm.dto.request.RoomUnitBulkRequest;
import com.tech10x.ukm.dto.request.RoomUnitRequest;
import com.tech10x.ukm.dto.request.RoomUnitUpdateRequest;
import com.tech10x.ukm.dto.response.RoomAvailabilityResponse;
import com.tech10x.ukm.dto.response.RoomCalendarResponse;
import com.tech10x.ukm.dto.response.RoomUnitResponse;
import com.tech10x.ukm.entity.RoomAvailability;
import com.tech10x.ukm.entity.RoomStatus;
import com.tech10x.ukm.security.Actor;

import java.time.LocalDate;
import java.util.List;

/** ROOM - the numbered rooms of a property, and which of them are booked. */
public interface RoomUnitService {

    List<RoomUnitResponse> list(Actor actor, String propertyId, String roomTypeId, RoomStatus status);

    RoomUnitResponse create(Actor actor, String propertyId, String roomTypeId, RoomUnitRequest request);

    List<RoomUnitResponse> createBulk(Actor actor, String propertyId, String roomTypeId, RoomUnitBulkRequest request);

    RoomUnitResponse update(Actor actor, String propertyId, String roomId, RoomUnitUpdateRequest request);

    void delete(Actor actor, String propertyId, String roomId);

    /**
     * Every room (optionally of one room type) with BOOKED / AVAILABLE / UNAVAILABLE for the stay
     * from {@code from} (check-in) to {@code to} (check-out). Defaults: from = today, to = from + 1 night.
     */
    RoomAvailabilityResponse availability(Actor actor, String propertyId, String roomTypeId,
                                          LocalDate from, LocalDate to, RoomAvailability filter);

    /** One room, night by night, for every date from {@code from} to {@code to} inclusive. */
    RoomCalendarResponse calendar(Actor actor, String propertyId, String roomId, LocalDate from, LocalDate to);
}
