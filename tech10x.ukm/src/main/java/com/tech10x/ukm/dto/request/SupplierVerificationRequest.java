package com.tech10x.ukm.dto.request;

import com.tech10x.ukm.entity.VerificationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** Admin request to move a supplier to PENDING / VERIFIED / REJECTED. */
@Getter
@Setter
public class SupplierVerificationRequest {

    @NotNull(message = "status is required (PENDING, VERIFIED or REJECTED)")
    private VerificationStatus status;
}
