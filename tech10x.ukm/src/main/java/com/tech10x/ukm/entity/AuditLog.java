package com.tech10x.ukm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/**
 * "28. AUDIT_LOG" - insert-only trail of sensitive/admin changes.
 * {@link Immutable} makes Hibernate ignore updates, and {@code AuditLogRepository} only exposes
 * save(), so rows can be neither modified nor deleted through the application.
 * Never put sensitive values (bank numbers, ID numbers, passwords) in old/new value.
 */
@Entity
@Immutable
@Table(name = "ukm_tbl_audit_log",
        indexes = @Index(name = "idx_audit_entity", columnList = "entity_name, entity_id"))
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nullable for system actions. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", updatable = false,
            foreignKey = @ForeignKey(name = "fk_audit_user"))
    private Users user;

    @Column(name = "entity_name", nullable = false, length = 60, updatable = false)
    private String entityName;

    /** String because public identifiers (PRP..., RMT...) are strings. */
    @Column(name = "entity_id", nullable = false, length = 60, updatable = false)
    private String entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private AuditAction action;

    @Column(name = "old_value", columnDefinition = "json", updatable = false)
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "json", updatable = false)
    private String newValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
