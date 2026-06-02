package com.medvault.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Returned when a patient cancels an appointment.
 * Carries the refund tier, a message, and whether the slot is freed.
 */
@Data @Builder
public class CancellationResponse {

    private Long   appointmentId;
    private String status;           // always "CANCELLED"

    /** FULL | PARTIAL | NONE */
    private String refundType;

    /** Human-readable description of the refund policy that applied */
    private String refundMessage;

    /** Hours between cancellation and appointment */
    private long   hoursBeforeAppointment;

    /** true = slot bookedCount was decremented and slot is bookable again */
    private boolean slotFreed;
}