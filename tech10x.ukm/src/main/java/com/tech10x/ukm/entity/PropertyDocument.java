package com.tech10x.ukm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * "9. PROPERTY_DOCUMENT" - verification documents (GST / PAN / agreement / ownership proof).
 * These are sensitive: {@code fileUrl} is only ever returned to the owning supplier and admins,
 * never on public endpoints.
 */
@Entity
@Table(name = "ukm_tbl_property_documents",
        indexes = @Index(name = "idx_document_property", columnList = "property_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"property", "fileUrl"})
public class PropertyDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false, unique = true, length = 40, updatable = false)
    private String documentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_document_property"))
    private Property property;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 30)
    private DocumentType documentType;

    @Column(name = "file_url", nullable = false, length = 255)
    private String fileUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    void onCreate() {
        this.uploadedAt = LocalDateTime.now();
        if (this.documentId == null) {
            this.documentId = "DOC" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        }
    }
}
