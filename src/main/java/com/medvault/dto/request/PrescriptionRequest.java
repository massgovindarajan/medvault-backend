package com.medvault.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class PrescriptionRequest {
    private Long   appointmentId;
    private String diagnosis;
    private String notes;
    private List<PrescriptionItemRequest> items;
}