package com.medvault.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SlotRequest {
    @NotNull private Long   doctorId;
    @NotNull private String date;        // "2026-03-15"
    @NotNull private String startTime;   // "09:00"
    @NotNull private String endTime;     // "17:00"
    @NotNull private Integer duration;   // minutes
    private Integer maxPatients;
    private Boolean isActive;
}