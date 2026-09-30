package com.tech10x.ukm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AmenityRequest {

    @NotBlank(message = "name is required")
    @Size(max = 60, message = "name must be at most 60 characters")
    private String name;

    /** An icon key such as "wifi" - never markup or a URL. */
    @Size(max = 60, message = "icon must be at most 60 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]*$", message = "icon may contain only letters, digits, '_' and '-'")
    private String icon;
}
