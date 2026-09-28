package com.tech10x.ukm.service;

import com.tech10x.ukm.dto.request.SupplierRegisterRequest;
import com.tech10x.ukm.dto.response.SupplierResponse;
import com.tech10x.ukm.entity.VerificationStatus;

import java.util.List;

public interface SupplierService {

    /** Creates the USER (role SUPPLIER) and its linked SUPPLIER profile in one transaction. */
    SupplierResponse registerSupplier(SupplierRegisterRequest request);

    /** Profile of the logged-in supplier, looked up through the user FK. */
    SupplierResponse getMyProfile(String userId);

    SupplierResponse getBySupplierId(String supplierId);

    /** All suppliers, or only those in the given verification status when status is non-null. */
    List<SupplierResponse> list(VerificationStatus status);

    SupplierResponse updateVerificationStatus(String supplierId, VerificationStatus status);
}
