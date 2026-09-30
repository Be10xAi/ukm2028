package com.tech10x.ukm.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class PropertyAmenitiesRequest {

    /** The complete set of amenities for the property (an empty set clears them). */
    @NotNull(message = "amenityIds is required")
    @Size(max = 60, message = "at most 60 amenities can be selected")
    private Set<Integer> amenityIds;
}
