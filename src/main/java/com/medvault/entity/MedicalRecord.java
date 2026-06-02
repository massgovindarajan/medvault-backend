package com.medvault.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "medical_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @Column(nullable = false, length = 50)
    private String category;       // PRESCRIPTION, TEST_REPORT, VACCINATION, HABIT, OTHER

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_name", length = 200)
    private String fileName;

    @Column(name = "file_type", length = 50)
    private String fileType;       // "application/pdf", "image/jpeg", etc.

    // HABIT fields — no file needed
    @Column(name = "habit_value", length = 200)
    private String habitValue;     // e.g. "Smokes 5 cigarettes/day"

    @Column(name = "is_sensitive")
    @Builder.Default
    private Boolean isSensitive = false;  // true → requires consent before doctor can view

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}