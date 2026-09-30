package com.tech10x.ukm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/** "8. PROPERTY_IMAGE" - one row per photo; the URL points at Azure Blob Storage. */
@Entity
@Table(name = "ukm_tbl_property_images",
        indexes = @Index(name = "idx_image_property", columnList = "property_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "property")
public class PropertyImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_id", nullable = false, unique = true, length = 40, updatable = false)
    private String imageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_image_property"))
    private Property property;

    @Column(nullable = false, length = 255)
    private String url;

    @Column(name = "is_cover", nullable = false)
    private boolean cover;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    void onCreate() {
        this.uploadedAt = LocalDateTime.now();
        if (this.imageId == null) {
            this.imageId = "IMG" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        }
    }
}
