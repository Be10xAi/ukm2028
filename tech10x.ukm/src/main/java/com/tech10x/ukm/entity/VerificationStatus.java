package com.tech10x.ukm.entity;

/**
 * SUPPLIER.verification_status - set by the admin verification workflow
 * after a supplier's KYC/business documents are reviewed.
 */
public enum VerificationStatus {
    PENDING,
    VERIFIED,
    REJECTED
}
