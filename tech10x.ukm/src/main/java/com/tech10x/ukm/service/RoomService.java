package com.tech10x.ukm.service;

import com.tech10x.ukm.dto.request.RoomInventoryRequest;
import com.tech10x.ukm.dto.request.RoomTypeRequest;
import com.tech10x.ukm.dto.response.PublicInventoryResponse;
import com.tech10x.ukm.dto.response.RoomInventoryResponse;
import com.tech10x.ukm.dto.response.RoomTypeResponse;
import com.tech10x.ukm.security.Actor;

import java.time.LocalDate;
import java.util.List;

/** ROOM_TYPE and ROOM_INVENTORY, always accessed through the owning property. */
public interface RoomService {

    List<RoomTypeResponse> listRoomTypes(Actor actor, String propertyId);

    RoomTypeResponse createRoomType(Actor actor, String propertyId, RoomTypeRequest request);

    RoomTypeResponse updateRoomType(Actor actor, String propertyId, String roomTypeId, RoomTypeRequest request);

    void deleteRoomType(Actor actor, String propertyId, String roomTypeId);

    /** Sets room counts for every date in [fromDate, toDate]; creates or updates one row per date. */
    List<RoomInventoryResponse> upsertInventory(Actor actor, String propertyId, String roomTypeId,
                                                RoomInventoryRequest request);

    List<RoomInventoryResponse> getInventory(Actor actor, String propertyId, String roomTypeId,
                                             LocalDate from, LocalDate to);

    /** Public availability of a LIVE property's room type. */
    List<PublicInventoryResponse> getPublicInventory(String propertyId, String roomTypeId,
                                                     LocalDate from, LocalDate to);
}
