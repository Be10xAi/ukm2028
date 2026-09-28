package com.tech10x.ukm.dto.response;

import com.tech10x.ukm.entity.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {
    private String supplierId;
    private String userId;
    private String name;
    private String email;
    private String mobileNo;
    private String businessName;
    private String contactPerson;
    private String gstNumber;
    private String panNumber;
    private String bankIfsc; // bankAccountNo is encrypted at rest and intentionally never returned
    private VerificationStatus verificationStatus;
    private LocalDateTime createdAt;
    private Set<String> roles;
}
