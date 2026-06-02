package com.medvault.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class AppointmentResponse {
    private Long    id;
    private Long    patientId;
    private String  patientName;
    private String  patientEmail;
    private String  patientPhone;
    private Long    doctorId;
    private String  doctorName;
    private String  specialization;
    private String  department;
    private String  appointmentDate;
    private String  appointmentTime;
    private String  symptoms;
    private String  duration;
    private String  severity;
    private Boolean previousVisit;
    private String  notes;
    private String  prescriptionUrl;
    private String  status;
    private String  rejectionReason;
    private String  createdAt;
}