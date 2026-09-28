package com.tech10x.ukm.entity;

import com.tech10x.ukm.security.CryptoConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Role-specific profile for a USER whose role = SUPPLIER
 * (hotel/dharamshala/camp owner) - see "3. SUPPLIER" in the DB design doc.
 * 1:1 with {@link Users}, linked back via user_id.
 */
@Entity
@Table(name = "ukm_tbl_suppliers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "bankAccountNo", "bankIfsc"})
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supplier_id", nullable = false, unique = true, length = 40)
    private String supplierId;

    /**
     * Real database foreign key: ukm_tbl_suppliers.user_id -> ukm_tbl_users.user_id.
     * One user can have at most one supplier profile (unique = true makes it 1:1).
     */
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id",
            nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_supplier_user"))
    private Users user;

    @Column(name = "business_name", nullable = false, length = 150)
    private String businessName;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    /** Nullable per doc - not every supplier has a GST registration. */
    @Column(name = "gst_number", length = 20)
    private String gstNumber;

    @Column(name = "pan_number", nullable = false, length = 15)
    private String panNumber;

    /**
     * Encrypted at rest (AES, via {@link CryptoConverter}) - must never appear
     * in plain text in logs or in AUDIT_LOG's old/new value JSON.
     */
    @Convert(converter = CryptoConverter.class)
    @Column(name = "bank_account_no", nullable = false, length = 255)
    private String bankAccountNo;

    @Column(name = "bank_ifsc", nullable = false, length = 15)
    private String bankIfsc;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.supplierId == null) {
            this.supplierId = "SUP" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        }
    }
}
