package com.tech10x.ukm.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Several rooms of one room type on the same floor in one call. */
@Getter
@Setter
public class RoomUnitBulkRequest {

    @NotEmpty(message = "roomNumbers is required")
    @Size(max = 100, message = "at most 100 rooms can be added at once")
    private List<@NotBlank(message = "room numbers cannot be blank")
            @Size(max = 20, message = "roomNumber must be at most 20 characters") String> roomNumbers;

    @Min(value = -5, message = "floor must be -5 or higher")
    @Max(value = 200, message = "floor must be at most 200")
    private Integer floor;
}
