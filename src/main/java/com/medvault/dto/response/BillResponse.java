package com.medvault.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class BillResponse {
    private Long    id;
    private String  billNumber;
    private Long    patientId;
    private String  patientName;
    private String  patientEmail;
    private String  patientPhone;
    private Long    appointmentId;
    private String  appointmentDate;
    private String  doctorName;
    private Long    prescriptionId;
    private String  status;            
    private String  paymentMethod;
    private Double  totalAmount;
    private Double  discountAmount;
    private Double  taxAmount;
    private Double  paidAmount;
    private Double  dueAmount;
    private String  transactionRef;
    private String  notes;
    private String  generatedByName;
    private String  paidByName;
    private String  paidAt;
    private String  createdAt;
    private List<BillItemResponse> items;
}