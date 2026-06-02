package com.medvault.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class BillRequest {
    private Long   patientId;
    private Long   appointmentId;    
    private Long   prescriptionId;   
    private String notes;
    private Double discountAmount;   
    private Double taxPercent;       
    private List<BillItemRequest> items;
}