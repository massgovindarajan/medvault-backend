package com.medvault.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String role;                  // "PATIENT", "DOCTOR", "ADMIN", etc.

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";    // "PENDING", "ACTIVE", "REJECTED"

    @Column(name = "password_reset_required")
    @Builder.Default
    private Boolean passwordResetRequired = true;

    @Column(name = "l1_approved")
    @Builder.Default
    private Boolean l1Approved = false;

    @Column(name = "l2_approved")
    @Builder.Default
    private Boolean l2Approved = false;

    @Column(name = "l1_approved_by")
    private Long l1ApprovedBy;

    @Column(name = "l2_approved_by")
    private Long l2ApprovedBy;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    // ✅ NEW: Controls which approval level this admin can access
    // 1 = L1 approvals only, 2 = L2 approvals only, null = full access (all tabs)
    @Column(name = "admin_level")
    private Integer adminLevel;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PersonalDetails personalDetails;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Address address;
}