package com.medvault.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class MedicalRecordResponse {
    private Long    id;
    private Long    patientId;
    private String  category;
    private String  title;
    private String  description;
    private String  fileName;
    private String  fileType;
    private String  fileUrl;
    private String  habitValue;
    private Boolean isSensitive;
    private String  createdAt;
}