package com.tech10x.ukm.dto.request;

import com.tech10x.ukm.entity.VerificationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentVerificationRequest {

    @NotNull(message = "status is required")
    private VerificationStatus status;
}
