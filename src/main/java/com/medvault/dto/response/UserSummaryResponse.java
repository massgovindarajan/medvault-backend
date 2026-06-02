package com.medvault.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryResponse {

    private Long          id;
    private String        email;
    private String        role;
    private String        status;
    private boolean       l1Approved;
    private boolean       l2Approved;
    private String        rejectionReason;
    private LocalDateTime createdAt;

    // From personal_details
    private String    firstName;
    private String    lastName;
    private String    phone;
    private LocalDate dob;
    private String    gender;
    private String    profilePhotoPath;

    // From address
    private String city;
    private String state;

    // Documents
    private List<DocumentItem>  identityDocuments;
    private List<DocumentItem>  medicalCertificates;
    private List<EducationItem> educationDetails;

    // ── Inner classes — separate Lombok annotations to avoid conflicts ──

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentItem {
        private String documentType;
        private String documentNumber;
        private String filePath;
        private String verifiedStatus;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EducationItem {
        private String degree;
        private String institution;
        private String yearOfCompletion;
        private String certificatePath;
    }
}