package com.medvault.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "identity_documents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IdentityDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;      // "AADHAAR", "PAN", "PASSPORT", etc.

    @Column(name = "document_number", nullable = false, length = 100)
    private String documentNumber;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "verified_status", length = 20)
    @Builder.Default
    private String verifiedStatus = "PENDING";  // "PENDING", "VERIFIED", "REJECTED"
}