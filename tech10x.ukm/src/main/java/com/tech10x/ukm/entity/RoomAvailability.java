package com.tech10x.ukm.entity;

/** What the owner sees for one physical room over a date range. Computed, never stored. */
public enum RoomAvailability {
    /** Sellable and not booked. */
    AVAILABLE,
    /** Has at least one confirmed booking overlapping the range. */
    BOOKED,
    /** Not booked, but cannot be sold because the room is in MAINTENANCE or INACTIVE. */
    UNAVAILABLE
}
