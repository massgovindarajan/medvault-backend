package com.medvault.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MedicalRecordRequest {
    @NotBlank private String category;    // PRESCRIPTION | TEST_REPORT | VACCINATION | HABIT | OTHER
    @NotBlank private String title;
    private String  description;
    private String  habitValue;           // only for HABIT category
    private Boolean isSensitive;
}