package com.medvault.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class PrescriptionResponse {
    private Long   id;
    private Long   appointmentId;
    private Long   doctorId;
    private String doctorName;
    private Long   patientId;
    private String patientName;
    private String patientEmail;
    private String diagnosis;
    private String notes;
    private String status;           
    private String createdAt;
    private String dispensedAt;
    private String dispensedByName;  
    private List<PrescriptionItemResponse> items;
}