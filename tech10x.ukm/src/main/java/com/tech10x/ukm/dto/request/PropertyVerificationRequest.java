package com.tech10x.ukm.dto.request;

import com.tech10x.ukm.entity.PropertyVerifiedStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyVerificationRequest {

    @NotNull(message = "verifiedStatus is required")
    private PropertyVerifiedStatus verifiedStatus;
}
