package com.medvault.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id")
    private DoctorSlot slot;

    @Column(name = "appointment_date", nullable = false, length = 20)
    private String appointmentDate;   // "2026-03-15"

    @Column(name = "appointment_time", nullable = false, length = 20)
    private String appointmentTime;   // "10:30 AM"

    @Column(length = 100)
    private String specialization;

    @Column(length = 100)
    private String department;

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(length = 50)
    private String duration;          // "1-3 days"

    @Column(length = 30)
    private String severity;          // "Mild", "Moderate", "Severe"

    @Column(name = "previous_visit")
    @Builder.Default
    private Boolean previousVisit = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "prescription_path", length = 500)
    private String prescriptionPath;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING"; // PENDING | CONFIRMED | REJECTED | COMPLETED

    @Column(name = "rejection_reason")
    private String rejectionReason;
    private String patientDisplayName;  // for walk-ins
    private String patientPhone;        // for walk-ins
    private String urgency;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}