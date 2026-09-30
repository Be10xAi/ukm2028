package com.tech10x.ukm.dto.request;

import com.tech10x.ukm.entity.PropertyStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyStatusRequest {

    @NotNull(message = "status is required")
    private PropertyStatus status;
}
