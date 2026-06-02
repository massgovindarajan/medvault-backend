package com.medvault.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConsentRequestDto {
    private Long   recordId;  // nullable = generic access request
    private String reason;
}