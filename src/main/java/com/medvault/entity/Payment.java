package com.medvault.entity;



//── Payment.java ──────────────────────────────────────────────────────────
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @ManyToOne
 @JoinColumn(name = "bill_id")
 private Bill bill;

 @Column(nullable = false)
 private String method;              // RAZORPAY, CASH, UPI, CARD

 @Column(name = "razorpay_payment_id")
 private String razorpayPaymentId;

 @Column(name = "razorpay_order_id")
 private String razorpayOrderId;

 @Column(name = "refund_id")
 private String refundId;

 @Column(name = "refunded_amount")
 private Integer refundedAmount;

 private Long amount;                // in rupees

 private String status;              // CAPTURED, FAILED, REFUNDED

 private String notes;

 @Column(name = "created_at")
 private LocalDateTime createdAt;

 @Column(name = "updated_at")
 private LocalDateTime updatedAt;

 @PrePersist
 void prePersist() { this.createdAt = LocalDateTime.now(); }

 @PreUpdate
 void preUpdate()  { this.updatedAt = LocalDateTime.now(); }
}



