package com.medvault.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "queue")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Queue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String tokenNumber;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    private String doctorName;

    @Enumerated(EnumType.STRING)
    private QueueStatus status;

    private LocalDateTime checkInTime;

    public enum QueueStatus {
        WAITING,
        IN_CONSULTATION,
        COMPLETED,
        MISSED
    }
}