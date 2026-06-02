
package com.medvault.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    // APPOINTMENT | PAYMENT | PRESCRIPTION | LAB | SYSTEM | QUEUE | BILLING
    @Column(nullable = false)
    private String type;

    // INFO | SUCCESS | WARNING | ERROR
    @Column(nullable = false)
    @Builder.Default
    private String severity = "INFO";

    // Link to navigate on click — e.g. /appointments/12
    @Column(name = "action_url")
    private String actionUrl;

    // Reference ID — appointmentId, billId, etc.
    @Column(name = "ref_id")
    private Long refId;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() { this.createdAt = LocalDateTime.now(); }
}