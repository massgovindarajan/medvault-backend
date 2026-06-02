package com.medvault.dto.request;

import lombok.Data;

@Data
public class AppointmentRequest {

    // ── Appointment core ──────────────────────────────────
    private Long   patientId;
    private Long   doctorId;
    private String doctorName;
    private String specialization;
    private String department;
    private String appointmentDate;   // YYYY-MM-DD
    private String appointmentTime;   // "9:00 AM"

    // ── Health information (from booking form step 3) ─────
    private String  symptoms;         // required
    private String  duration;         // e.g. "1–3 days"
    private String  severity;         // Mild | Moderate | Severe | Very Severe
    private Boolean previousVisit;    // visited this doctor before?
    private String  notes;            // additional notes (optional)
}