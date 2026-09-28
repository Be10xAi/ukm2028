package com.tech10x.ukm.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Registration payload for a USER whose role = SUPPLIER
 * (hotel/dharamshala/camp owner) - fields mirror "3. SUPPLIER" in the DB design doc.
 */
@Getter
@Setter
public class SupplierRegisterRequest {

    // ---- base account fields (same as RegisterRequest / USER) ----

    @NotBlank(message = "name is required")
    @Size(max = 60, message = "name must be at most 60 characters")
    private String name;

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email address")
    @Size(max = 120, message = "email must be at most 120 characters")
    private String userId;

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email address")
    @Size(max = 120, message = "email must be at most 120 characters")
    private String email;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "mobileNo must be 10-15 digits")
    private String mobileNo;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 72, message = "password must be between 8 and 72 characters")
    private String password;

    private LocalDate dob;

    private String gender;

    // ---- SUPPLIER profile fields ----

    @NotBlank(message = "businessName is required")
    @Size(max = 150, message = "businessName must be at most 150 characters")
    private String businessName;

    @Size(max = 100, message = "contactPerson must be at most 100 characters")
    private String contactPerson;

    /** Nullable per doc - not every supplier has a GST registration. */
    @Size(max = 20, message = "gstNumber must be at most 20 characters")
    private String gstNumber;

    @NotBlank(message = "panNumber is required")
    @Size(max = 15, message = "panNumber must be at most 15 characters")
    private String panNumber;

    @NotBlank(message = "bankAccountNo is required")
    @Size(max = 30, message = "bankAccountNo must be at most 30 characters")
    private String bankAccountNo;

    @NotBlank(message = "bankIfsc is required")
    @Size(max = 15, message = "bankIfsc must be at most 15 characters")
    private String bankIfsc;
}
