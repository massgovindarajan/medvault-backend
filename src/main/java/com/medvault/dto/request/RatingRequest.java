package com.medvault.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RatingRequest {
    @NotNull private Long    appointmentId;
    @NotNull private Long    doctorId;
    @NotNull @Min(1) @Max(5) private Integer stars;
    private String feedback;
}