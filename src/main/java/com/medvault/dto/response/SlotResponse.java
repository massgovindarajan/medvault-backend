package com.medvault.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class SlotResponse {
    private Long    id;
    private Long    doctorId;
    private String  doctorName;
    private String  date;
    private String  startTime;
    private String  endTime;
    private Integer duration;
    private Integer maxPatients;
    private Boolean isActive;
    private Integer bookedCount;
}