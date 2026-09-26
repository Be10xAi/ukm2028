package com.tech10x.ukm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "ukm_tbl_otp_token",
        indexes = @Index(name = "idx_otp_user_id", columnList = "user_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "otpHash")
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 40)
    private String userId;

    /** BCrypt hash of the OTP, never the raw code. */
    @Column(name = "otp_hash", nullable = false, length = 100)
    private String otpHash;

    @Column(name = "otp", nullable = false, length = 100)
    private String otp;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean used = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Builder.Default
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy="Admin";

    @Column(name = "no_of_attempt",nullable = false)
    private int noOfAttempt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
