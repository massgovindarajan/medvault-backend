//
//
//package com.medvault.service.impl;
//
//import com.medvault.dto.request.ApprovalRequest;
//import com.medvault.dto.response.ApiResponse;
//import com.medvault.dto.response.UserSummaryResponse;
//import com.medvault.entity.User;
//import com.medvault.repository.*;
//import com.medvault.service.AdminService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class AdminServiceImpl implements AdminService {
//
//    private final UserRepository               userRepository;
//    private final PersonalDetailsRepository    personalDetailsRepository;
//    private final AddressRepository            addressRepository;
//    private final IdentityDocumentRepository   identityDocumentRepository;
//    private final MedicalCertificateRepository medicalCertificateRepository;
//    private final EducationDetailsRepository   educationDetailsRepository;
//    private final WorkExperienceRepository     workExperienceRepository;     // ✅ ADDED
//    private final DoctorSlotRepository         doctorSlotRepository;
//    private final ChatMessageRepository        chatMessageRepository;
//    private final AppointmentRepository        appointmentRepository;
//    private final MedicalRecordRepository      medicalRecordRepository;
//    private final PasswordEncoder              passwordEncoder;
//    private final AuthServiceImpl              authServiceImpl;
//
//    @Override
//    public List<UserSummaryResponse> getL1PendingUsers() {
//        return userRepository.findByL1ApprovedFalseAndStatus("PENDING")
//                .stream().map(this::toSummary).collect(Collectors.toList());
//    }
//
//    @Override
//    public List<UserSummaryResponse> getL2PendingUsers() {
//        return userRepository.findByL1ApprovedTrueAndL2ApprovedFalseAndStatus("PENDING")
//                .stream().map(this::toSummary).collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional
//    public ApiResponse<String> l1Approve(Long adminId, ApprovalRequest req) {
//        User user = userRepository.findById(req.getUserId())
//                .orElseThrow(() -> new RuntimeException("User not found: " + req.getUserId()));
//        if (req.isApproved()) {
//            user.setL1Approved(true);
//            user.setL1ApprovedBy(adminId);
//            userRepository.save(user);
//            return ApiResponse.ok("L1 approval granted for: " + user.getEmail());
//        } else {
//            user.setStatus("REJECTED");
//            user.setRejectionReason(req.getRejectionReason());
//            userRepository.save(user);
//            return ApiResponse.ok("User rejected: " + user.getEmail());
//        }
//    }
//
//    @Override
//    @Transactional
//    public ApiResponse<String> l2Approve(Long adminId, ApprovalRequest req) {
//        User user = userRepository.findById(req.getUserId())
//                .orElseThrow(() -> new RuntimeException("User not found: " + req.getUserId()));
//        if (!Boolean.TRUE.equals(user.getL1Approved())) {
//            return ApiResponse.error("L1 approval is still pending.");
//        }
//        if (req.isApproved()) {
//            user.setL2Approved(true);
//            user.setL2ApprovedBy(adminId);
//            user.setStatus("ACTIVE");
//            String tempPwd = authServiceImpl.generateTempPassword(user);
//            user.setPassword(passwordEncoder.encode(tempPwd));
//            user.setPasswordResetRequired(true);
//            userRepository.save(user);
//
//            identityDocumentRepository.findByUserId(user.getId()).forEach(doc -> {
//                doc.setVerifiedStatus("VERIFIED");
//                identityDocumentRepository.save(doc);
//            });
//
//            medicalCertificateRepository.findByUserId(user.getId()).forEach(cert -> {
//                cert.setVerificationStatus("VERIFIED");
//                medicalCertificateRepository.save(cert);
//            });
//
//            log.info("Account ACTIVATED: {} | TempPwd: {}", user.getEmail(), tempPwd);
//            return ApiResponse.ok("Account ACTIVATED: " + user.getEmail() + " | Temp Password: " + tempPwd);
//        } else {
//            user.setStatus("REJECTED");
//            user.setRejectionReason(req.getRejectionReason());
//            userRepository.save(user);
//            return ApiResponse.ok("User rejected: " + user.getEmail());
//        }
//    }
//
//    @Override
//    public List<UserSummaryResponse> getAllUsers() {
//        return userRepository.findAll()
//                .stream().map(this::toSummary).collect(Collectors.toList());
//    }
//
//    @Override
//    public UserSummaryResponse getUserDetail(Long userId) {
//        return toSummary(userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found")));
//    }
//
//    /**
//     * Hard-delete a user and ALL child rows that reference users.id
//     *
//     * Complete FK chain (delete children BEFORE parent):
//     *   chat_messages        → users.id  (user_id)
//     *   appointments         → users.id  (patient_id + doctor_id)
//     *   medical_records      → users.id  (patient_id)
//     *   doctor_slots         → users.id  (doctor_id)
//     *   work_experience      → users.id  (user_id)   ✅ ADDED
//     *   identity_documents   → users.id
//     *   medical_certificates → users.id
//     *   education_details    → users.id
//     *   personal_details     → users.id
//     *   addresses            → users.id
//     */
//    @Override
//    @Transactional
//    public ApiResponse<String> deleteUser(Long userId) {
//        if (!userRepository.existsById(userId)) {
//            return ApiResponse.error("User not found: " + userId);
//        }
//
//        // ── Delete all FK children first (order matters) ──────────────
//        chatMessageRepository.deleteByUserId(userId);
//        appointmentRepository.deleteByPatientId(userId);
//        appointmentRepository.deleteByDoctorId(userId);
//        medicalRecordRepository.deleteByPatientId(userId);
//        doctorSlotRepository.deleteByDoctorId(userId);
//        workExperienceRepository.deleteByUserId(userId);        // ✅ ADDED
//        identityDocumentRepository.deleteByUserId(userId);
//        medicalCertificateRepository.deleteByUserId(userId);
//        educationDetailsRepository.deleteByUserId(userId);
//        personalDetailsRepository.deleteByUserId(userId);
//        addressRepository.deleteByUserId(userId);
//
//        // ── Finally delete the user row ───────────────────────────────
//        userRepository.deleteById(userId);
//
//        log.info("User {} fully deleted by admin", userId);
//        return ApiResponse.ok("User deleted successfully.");
//    }
//
//    // ── Mapper ────────────────────────────────────────────────────────
//    private UserSummaryResponse toSummary(User user) {
//        UserSummaryResponse r = UserSummaryResponse.builder()
//                .id(user.getId())
//                .email(user.getEmail())
//                .role(user.getRole())
//                .status(user.getStatus())
//                .l1Approved(Boolean.TRUE.equals(user.getL1Approved()))
//                .l2Approved(Boolean.TRUE.equals(user.getL2Approved()))
//                .rejectionReason(user.getRejectionReason())
//                .createdAt(user.getCreatedAt())
//                .build();
//
//        personalDetailsRepository.findByUserId(user.getId()).ifPresent(pd -> {
//            r.setFirstName(pd.getFirstName());
//            r.setLastName(pd.getLastName());
//            r.setPhone(pd.getPhone());
//            r.setDob(pd.getDob());
//            r.setGender(pd.getGender());
//            r.setProfilePhotoPath(pd.getProfilePhotoPath());
//        });
//
//        addressRepository.findByUserId(user.getId()).ifPresent(a -> {
//            r.setCity(a.getCity());
//            r.setState(a.getState());
//        });
//
//        boolean isActive = "ACTIVE".equals(user.getStatus()) && Boolean.TRUE.equals(user.getL2Approved());
//
//        List<UserSummaryResponse.DocumentItem> idDocs = identityDocumentRepository.findByUserId(user.getId())
//                .stream()
//                .map(d -> UserSummaryResponse.DocumentItem.builder()
//                        .documentType(d.getDocumentType())
//                        .documentNumber(d.getDocumentNumber())
//                        .filePath(d.getFilePath())
//                        .verifiedStatus(isActive ? "VERIFIED" : d.getVerifiedStatus())
//                        .build())
//                .collect(Collectors.toList());
//        r.setIdentityDocuments(idDocs);
//
//        List<UserSummaryResponse.DocumentItem> certs = medicalCertificateRepository.findByUserId(user.getId())
//                .stream()
//                .map(c -> UserSummaryResponse.DocumentItem.builder()
//                        .documentType("Medical Licence")
//                        .documentNumber(c.getLicenseNumber())
//                        .filePath(c.getCertificateFilePath())
//                        .verifiedStatus(isActive ? "VERIFIED" : c.getVerificationStatus())
//                        .build())
//                .collect(Collectors.toList());
//        r.setMedicalCertificates(certs);
//
//        List<UserSummaryResponse.EducationItem> edu = educationDetailsRepository.findByUserId(user.getId())
//                .stream()
//                .map(e -> UserSummaryResponse.EducationItem.builder()
//                        .degree(e.getDegree())
//                        .institution(e.getInstitution())
//                        .yearOfCompletion(e.getYearOfCompletion() != null ? e.getYearOfCompletion().toString() : "")
//                        .certificatePath(e.getCertificatePath())
//                        .build())
//                .collect(Collectors.toList());
//        r.setEducationDetails(edu);
//
//        return r;
//    }
//}
package com.medvault.service.impl;

import com.medvault.dto.request.ApprovalRequest;
import com.medvault.dto.response.ApiResponse;
import com.medvault.dto.response.UserSummaryResponse;
import com.medvault.entity.User;
import com.medvault.repository.*;
import com.medvault.service.AdminService;
import com.medvault.service.AuthService;              // ← interface, not impl
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository               userRepository;
    private final PersonalDetailsRepository    personalDetailsRepository;
    private final AddressRepository            addressRepository;
    private final IdentityDocumentRepository   identityDocumentRepository;
    private final MedicalCertificateRepository medicalCertificateRepository;
    private final EducationDetailsRepository   educationDetailsRepository;
    private final WorkExperienceRepository     workExperienceRepository;
    private final DoctorSlotRepository         doctorSlotRepository;
    private final ChatMessageRepository        chatMessageRepository;
    private final AppointmentRepository        appointmentRepository;
    private final MedicalRecordRepository      medicalRecordRepository;
    private final PasswordEncoder              passwordEncoder;
    private final AuthService                  authService;             // ← interface

    @Override
    public List<UserSummaryResponse> getL1PendingUsers() {
        return userRepository.findByL1ApprovedFalseAndStatus("PENDING")
                .stream().map(this::toSummary).collect(Collectors.toList());
    }

    @Override
    public List<UserSummaryResponse> getL2PendingUsers() {
        return userRepository.findByL1ApprovedTrueAndL2ApprovedFalseAndStatus("PENDING")
                .stream().map(this::toSummary).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ApiResponse<String> l1Approve(Long adminId, ApprovalRequest req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + req.getUserId()));
        if (req.isApproved()) {
            user.setL1Approved(true);
            user.setL1ApprovedBy(adminId);
            userRepository.save(user);
            return ApiResponse.ok("L1 approval granted for: " + user.getEmail());
        } else {
            user.setStatus("REJECTED");
            user.setRejectionReason(req.getRejectionReason());
            userRepository.save(user);
            return ApiResponse.ok("User rejected: " + user.getEmail());
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> l2Approve(Long adminId, ApprovalRequest req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + req.getUserId()));
        if (!Boolean.TRUE.equals(user.getL1Approved())) {
            return ApiResponse.error("L1 approval is still pending.");
        }
        if (req.isApproved()) {
            user.setL2Approved(true);
            user.setL2ApprovedBy(adminId);
            user.setStatus("ACTIVE");
            String tempPwd = authService.generateTempPassword(user);  // ← interface call
            user.setPassword(passwordEncoder.encode(tempPwd));
            user.setPasswordResetRequired(true);
            userRepository.save(user);

            identityDocumentRepository.findByUserId(user.getId()).forEach(doc -> {
                doc.setVerifiedStatus("VERIFIED");
                identityDocumentRepository.save(doc);
            });

            medicalCertificateRepository.findByUserId(user.getId()).forEach(cert -> {
                cert.setVerificationStatus("VERIFIED");
                medicalCertificateRepository.save(cert);
            });

            log.info("Account ACTIVATED: {} | TempPwd: {}", user.getEmail(), tempPwd);
            return ApiResponse.ok("Account ACTIVATED: " + user.getEmail() + " | Temp Password: " + tempPwd);
        } else {
            user.setStatus("REJECTED");
            user.setRejectionReason(req.getRejectionReason());
            userRepository.save(user);
            return ApiResponse.ok("User rejected: " + user.getEmail());
        }
    }

    @Override
    public List<UserSummaryResponse> getAllUsers() {
        return userRepository.findAll()
                .stream().map(this::toSummary).collect(Collectors.toList());
    }

    @Override
    public UserSummaryResponse getUserDetail(Long userId) {
        return toSummary(userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")));
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            return ApiResponse.error("User not found: " + userId);
        }

        chatMessageRepository.deleteByUserId(userId);
        appointmentRepository.deleteByPatientId(userId);
        appointmentRepository.deleteByDoctorId(userId);
        medicalRecordRepository.deleteByPatientId(userId);
        doctorSlotRepository.deleteByDoctorId(userId);
        workExperienceRepository.deleteByUserId(userId);
        identityDocumentRepository.deleteByUserId(userId);
        medicalCertificateRepository.deleteByUserId(userId);
        educationDetailsRepository.deleteByUserId(userId);
        personalDetailsRepository.deleteByUserId(userId);
        addressRepository.deleteByUserId(userId);

        userRepository.deleteById(userId);

        log.info("User {} fully deleted by admin", userId);
        return ApiResponse.ok("User deleted successfully.");
    }

    // ── Mapper ────────────────────────────────────────────────────────────────
    private UserSummaryResponse toSummary(User user) {
        UserSummaryResponse r = UserSummaryResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .l1Approved(Boolean.TRUE.equals(user.getL1Approved()))
                .l2Approved(Boolean.TRUE.equals(user.getL2Approved()))
                .rejectionReason(user.getRejectionReason())
                .createdAt(user.getCreatedAt())
                .build();

        personalDetailsRepository.findByUserId(user.getId()).ifPresent(pd -> {
            r.setFirstName(pd.getFirstName());
            r.setLastName(pd.getLastName());
            r.setPhone(pd.getPhone());
            r.setDob(pd.getDob());
            r.setGender(pd.getGender());
            r.setProfilePhotoPath(pd.getProfilePhotoPath());
        });

        addressRepository.findByUserId(user.getId()).ifPresent(a -> {
            r.setCity(a.getCity());
            r.setState(a.getState());
        });

        boolean isActive = "ACTIVE".equals(user.getStatus()) && Boolean.TRUE.equals(user.getL2Approved());

        List<UserSummaryResponse.DocumentItem> idDocs = identityDocumentRepository.findByUserId(user.getId())
                .stream()
                .map(d -> UserSummaryResponse.DocumentItem.builder()
                        .documentType(d.getDocumentType())
                        .documentNumber(d.getDocumentNumber())
                        .filePath(d.getFilePath())
                        .verifiedStatus(isActive ? "VERIFIED" : d.getVerifiedStatus())
                        .build())
                .collect(Collectors.toList());
        r.setIdentityDocuments(idDocs);

        List<UserSummaryResponse.DocumentItem> certs = medicalCertificateRepository.findByUserId(user.getId())
                .stream()
                .map(c -> UserSummaryResponse.DocumentItem.builder()
                        .documentType("Medical Licence")
                        .documentNumber(c.getLicenseNumber())
                        .filePath(c.getCertificateFilePath())
                        .verifiedStatus(isActive ? "VERIFIED" : c.getVerificationStatus())
                        .build())
                .collect(Collectors.toList());
        r.setMedicalCertificates(certs);

        List<UserSummaryResponse.EducationItem> edu = educationDetailsRepository.findByUserId(user.getId())
                .stream()
                .map(e -> UserSummaryResponse.EducationItem.builder()
                        .degree(e.getDegree())
                        .institution(e.getInstitution())
                        .yearOfCompletion(e.getYearOfCompletion() != null ? e.getYearOfCompletion().toString() : "")
                        .certificatePath(e.getCertificatePath())
                        .build())
                .collect(Collectors.toList());
        r.setEducationDetails(edu);

        return r;
    }
}