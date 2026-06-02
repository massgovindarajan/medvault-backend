package com.medvault.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ConsentRequestResponse {
    private Long   id;
    private Long   doctorId;
    private String doctorName;
    private Long   patientId;
    private String patientName;
    private Long   recordId;
    private String recordTitle;
    private String recordCategory;
    private String status;
    private String reason;
    private String requestedAt;
    private String respondedAt;
}