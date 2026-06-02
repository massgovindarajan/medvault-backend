package com.medvault.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * One medicine line on a prescription.
 * Status: PENDING | DISPENSED | OUT_OF_STOCK
 */
@Entity
@Table(name = "prescription_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PrescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @Column(name = "medicine_name", nullable = false, length = 200)
    private String medicineName;

    @Column(length = 100)
    private String dosage;       // e.g. "500mg"

    @Column(length = 100)
    private String frequency;    // e.g. "Twice daily"

    @Column(length = 50)
    private String duration;     // e.g. "5 days"

    @Column(length = 200)
    private String instructions; // e.g. "Take after food"

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING"; // PENDING | DISPENSED | OUT_OF_STOCK

    /** Quantity to dispense */
    @Builder.Default
    private Integer quantity = 1;
}