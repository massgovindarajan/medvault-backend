package com.medvault.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_slots")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DoctorSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false, length = 5)
    private String startTime;     // "09:00"

    @Column(name = "end_time", nullable = false, length = 5)
    private String endTime;       // "17:00"

    @Column(nullable = false)
    private Integer duration;     // minutes per slot

    @Column(name = "max_patients", nullable = false)
    @Builder.Default
    private Integer maxPatients = 1;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "booked_count", nullable = false)
    @Builder.Default
    private Integer bookedCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}