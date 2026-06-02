package com.medvault.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LoginResponse {
    private String  token;
    private Long    userId;
    private String  email;
    private String  role;                   // plain String: "DOCTOR", "PATIENT", etc.
    private String  status;                 // plain String: "ACTIVE", "PENDING", "REJECTED"
    private boolean passwordResetRequired;
    private String  message;
    private Integer adminLevel;             // ✅ NEW: 1=L1 only, 2=L2 only, null=full admin
}