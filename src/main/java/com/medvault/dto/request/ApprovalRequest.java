package com.medvault.dto.request;

import lombok.Data;

@Data
public class ApprovalRequest {
    private Long userId;
    private boolean approved;
    private String rejectionReason;
}