package com.tech10x.ukm.dto.request;

import com.tech10x.ukm.entity.RoomStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomUnitUpdateRequest {

    @NotBlank(message = "roomNumber is required")
    @Size(max = 20, message = "roomNumber must be at most 20 characters")
    private String roomNumber;

    @Min(value = -5, message = "floor must be -5 or higher")
    @Max(value = 200, message = "floor must be at most 200")
    private Integer floor;

    @NotNull(message = "status is required")
    private RoomStatus status;
}
