package com.medvault.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class RatingResponse {
    private Long   id;
    private Long   appointmentId;
    private Long   doctorId;
    private Long   patientId;
    private String patientName;
    private Integer stars;
    private String feedback;
    private String ratedAt;
}