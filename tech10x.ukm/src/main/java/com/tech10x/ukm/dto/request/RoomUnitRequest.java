package com.tech10x.ukm.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** One physical room, e.g. {"roomNumber": "101", "floor": 1}. */
@Getter
@Setter
public class RoomUnitRequest {

    @NotBlank(message = "roomNumber is required")
    @Size(max = 20, message = "roomNumber must be at most 20 characters")
    private String roomNumber;

    @Min(value = -5, message = "floor must be -5 or higher")
    @Max(value = 200, message = "floor must be at most 200")
    private Integer floor;
}
