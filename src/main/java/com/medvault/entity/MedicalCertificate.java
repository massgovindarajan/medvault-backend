package com.medvault.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "medical_certificates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicalCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "license_number", nullable = false, length = 100)
    private String licenseNumber;

    @Column(name = "registration_council", length = 200)
    private String registrationCouncil;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "certificate_file_path", length = 500)
    private String certificateFilePath;

    @Column(name = "verification_status", length = 20)
    @Builder.Default
    private String verificationStatus = "PENDING";  // "PENDING", "VERIFIED", "REJECTED"
}