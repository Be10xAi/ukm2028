package com.tech10x.ukm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** The full set of room numbers a booking should hold - exactly as many as the booking has rooms. */
@Getter
@Setter
public class RoomAssignmentRequest {

    @NotEmpty(message = "roomNumbers is required")
    @Size(max = 50, message = "at most 50 rooms per booking")
    private List<@NotBlank(message = "room numbers cannot be blank")
            @Size(max = 20, message = "roomNumber must be at most 20 characters") String> roomNumbers;
}
