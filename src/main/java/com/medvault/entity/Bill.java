//package com.medvault.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * A Bill is generated after doctor consultation + pharmacy processing.
// * Status flow: DRAFT → PENDING → PAID | CANCELLED
// * Payment methods: CASH | CARD | UPI | ONLINE
// */
//@Entity
//@Table(name = "bills")
//@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
//public class Bill {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    /** Auto-generated bill number e.g. BILL-2026-00042 */
//    @Column(name = "bill_number", unique = true, nullable = false, length = 30)
//    private String billNumber;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "patient_id", nullable = false)
//    private User patient;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "appointment_id")
//    private Appointment appointment;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "prescription_id")
//    private Prescription prescription;
//
//    /** DRAFT | PENDING | PAID | CANCELLED */
//    @Column(nullable = false, length = 20)
//    @Builder.Default
//    private String status = "PENDING";
//
//    /** CASH | CARD | UPI | ONLINE */
//    @Column(name = "payment_method", length = 20)
//    private String paymentMethod;
//
//    @Column(name = "total_amount", nullable = false)
//    @Builder.Default
//    private Double totalAmount = 0.0;
//
//    @Column(name = "discount_amount")
//    @Builder.Default
//    private Double discountAmount = 0.0;
//
//    @Column(name = "tax_amount")
//    @Builder.Default
//    private Double taxAmount = 0.0;
//
//    @Column(name = "paid_amount")
//    @Builder.Default
//    private Double paidAmount = 0.0;
//
//    @Column(name = "due_amount")
//    @Builder.Default
//    private Double dueAmount = 0.0;
//
//    /** Transaction reference (UPI/Card/Online) */
//    @Column(name = "transaction_ref", length = 100)
//    private String transactionRef;
//
//    @Column(columnDefinition = "TEXT")
//    private String notes;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "generated_by")
//    private User generatedBy;   // Receptionist / Billing staff
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "paid_by")
//    private User paidBy;        // who collected payment
//
//    @Column(name = "paid_at")
//    private LocalDateTime paidAt;
//
//    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @Builder.Default
//    private List<BillItem> items = new ArrayList<>();
//
//    @CreationTimestamp
//    @Column(name = "created_at", updatable = false)
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//    
//    @Column(name = "bill_ref", unique = true)
//    private String billRef;
// 
//    @Column(name = "payment_reference")
//    private String paymentReference;    // Razorpay paymentId
// 
//
// 
//    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL)
//    private List<Payment> payments;
//}
package com.medvault.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bills")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bill_number", unique = true, nullable = false)
    private String billNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id")
    private Prescription prescription;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BillItem> items;

    // ── Amounts ──────────────────────────────────────────────────────────
    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "discount_amount")
    private Double discountAmount;

    @Column(name = "tax_amount")
    private Double taxAmount;

    @Column(name = "paid_amount")
    private Double paidAmount;

    @Column(name = "due_amount")
    private Double dueAmount;

    // ── Status & Payment ─────────────────────────────────────────────────
    // PENDING | PAID | CANCELLED | REFUNDED
    @Column(nullable = false)
    private String status;

    @Column(name = "payment_method")
    private String paymentMethod;           // CASH, CARD, UPI, ONLINE

    @Column(name = "transaction_ref")
    private String transactionRef;

    // ── Staff ────────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by")
    private User generatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by")
    private User paidBy;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    private String notes;

    // ── Audit ────────────────────────────────────────────────────────────
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null)        this.status        = "PENDING";
        if (this.paidAmount   == null)  this.paidAmount    = 0.0;
        if (this.discountAmount == null) this.discountAmount = 0.0;
        if (this.taxAmount    == null)  this.taxAmount     = 0.0;
    }

    @PreUpdate
    void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}