package com.medvault.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Personal Info ─────────────────────────────────────────
    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    // ── Address ───────────────────────────────────────────────
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private String country;

    // ── Medical Info ──────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    private BloodGroup bloodGroup;

    private String allergies;

    private String medicalHistory;

    private String emergencyContactName;
    private String emergencyContactPhone;

    // ── Auth ──────────────────────────────────────────────────
    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    // ── Audit ─────────────────────────────────────────────────
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ── Enums ─────────────────────────────────────────────────
    public enum Gender {
        MALE, FEMALE, OTHER
    }

    public enum BloodGroup {
        A_POS, A_NEG,
        B_POS, B_NEG,
        AB_POS, AB_NEG,
        O_POS, O_NEG
    }
}