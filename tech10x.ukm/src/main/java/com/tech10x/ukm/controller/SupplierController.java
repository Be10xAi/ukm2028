package com.tech10x.ukm.controller;

import com.tech10x.ukm.dto.request.SupplierRegisterRequest;
import com.tech10x.ukm.dto.request.SupplierVerificationRequest;
import com.tech10x.ukm.dto.response.GenericResponse;
import com.tech10x.ukm.dto.response.SupplierResponse;
import com.tech10x.ukm.entity.VerificationStatus;
import com.tech10x.ukm.service.SupplierService;
import com.tech10x.ukm.utils.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Supplier (hotel / dharamshala / camp owner) endpoints.
 * Each supplier is linked 1:1 to a user through the ukm_tbl_suppliers.user_id foreign key.
 */
@RestController
@RequestMapping("/api/v1/ukm/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    /** Public: creates the user (ROLE_SUPPLIER) and the linked supplier profile. */
    @PostMapping("/register")
    public ResponseEntity<GenericResponse<SupplierResponse>> register(
            @Valid @RequestBody SupplierRegisterRequest request) {
        SupplierResponse supplier = supplierService.registerSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success(List.of(supplier), "Supplier registered successfully"));
    }

    /** The logged-in supplier's own profile (resolved via the user FK from the JWT identity). */
    @GetMapping("/me")
    @PreAuthorize("hasRole('SUPPLIER')")
    public ResponseEntity<GenericResponse<SupplierResponse>> me(Authentication authentication) {
        SupplierResponse supplier = supplierService.getMyProfile(authentication.getName());
        return ResponseEntity.ok(ResponseUtil.success(List.of(supplier)));
    }

    /** Admin: list all suppliers, optionally filtered by ?status=PENDING|VERIFIED|REJECTED. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GenericResponse<SupplierResponse>> list(
            @RequestParam(required = false) VerificationStatus status) {
        return ResponseEntity.ok(ResponseUtil.success(supplierService.list(status)));
    }

    /** Admin: fetch one supplier by its supplierId. */
    @GetMapping("/{supplierId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GenericResponse<SupplierResponse>> get(@PathVariable String supplierId) {
        return ResponseEntity.ok(ResponseUtil.success(List.of(supplierService.getBySupplierId(supplierId))));
    }

    /** Admin: approve or reject a supplier's verification. */
    @PatchMapping("/{supplierId}/verification")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GenericResponse<SupplierResponse>> updateVerification(
            @PathVariable String supplierId,
            @Valid @RequestBody SupplierVerificationRequest request) {
        SupplierResponse supplier = supplierService.updateVerificationStatus(supplierId, request.getStatus());
        return ResponseEntity.ok(ResponseUtil.success(List.of(supplier), "Verification status updated"));
    }
}
