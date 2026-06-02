package com.medvault.service.impl;

import com.medvault.dto.request.ConsentActionRequest;
import com.medvault.dto.request.ConsentRequestDto;
import com.medvault.dto.request.MedicalRecordRequest;
import com.medvault.dto.response.ApiResponse;
import com.medvault.dto.response.ConsentRequestResponse;
import com.medvault.dto.response.MedicalRecordResponse;
import com.medvault.entity.ConsentRequest;
import com.medvault.entity.MedicalRecord;
import com.medvault.entity.Appointment;
import com.medvault.entity.User;
import com.medvault.repository.AppointmentRepository;
import com.medvault.repository.ConsentRequestRepository;
import com.medvault.repository.MedicalRecordRepository;
import com.medvault.repository.PersonalDetailsRepository;
import com.medvault.repository.UserRepository;
import com.medvault.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final UserRepository            userRepository;
    private final PersonalDetailsRepository personalDetailsRepository;
    private final MedicalRecordRepository   recordRepository;
    private final ConsentRequestRepository  consentRepository;
    private final AppointmentRepository     appointmentRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // ═══════════════════════════════════════════════════
    // PATIENT — RECORDS
    // ═══════════════════════════════════════════════════

    @Override
    @Transactional
    public ApiResponse<MedicalRecordResponse> uploadRecord(
            Long patientId, MedicalRecordRequest req, MultipartFile file) {

        User patient = getUser(patientId);

        String filePath = null, fileName = null, fileType = null;
        if (file != null && !file.isEmpty()) {
            filePath = saveFile(file, "records/" + patientId);
            fileName = file.getOriginalFilename();
            fileType = file.getContentType();
        }

        MedicalRecord record = MedicalRecord.builder()
                .patient(patient)
                .category(req.getCategory())
                .title(req.getTitle())
                .description(req.getDescription())
                .filePath(filePath)
                .fileName(fileName)
                .fileType(fileType)
                .habitValue(req.getHabitValue())
                .isSensitive(req.getIsSensitive() != null ? req.getIsSensitive() : false)
                .isActive(true)
                .build();

        MedicalRecord saved = recordRepository.save(record);
        log.info("Medical record uploaded: patient={}, category={}", patientId, req.getCategory());
        return ApiResponse.ok("Record uploaded successfully!", toRecordResponse(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getMyRecords(Long patientId) {
        return recordRepository
                .findByPatientIdAndIsActiveTrueOrderByCreatedAtDesc(patientId)
                .stream().map(this::toRecordResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getMyRecordsByCategory(Long patientId, String category) {
        return recordRepository
                .findByPatientIdAndCategoryAndIsActiveTrueOrderByCreatedAtDesc(patientId, category)
                .stream().map(this::toRecordResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteRecord(Long patientId, Long recordId) {
        MedicalRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found: " + recordId));

        if (!record.getPatient().getId().equals(patientId)) {
            return ApiResponse.error("You can only delete your own records.");
        }

        record.setIsActive(false);
        recordRepository.save(record);
        return ApiResponse.ok("Record deleted.");
    }

    // ═══════════════════════════════════════════════════
    // DOCTOR — CONSENT REQUEST
    // ═══════════════════════════════════════════════════

    @Override
    @Transactional
    public ApiResponse<String> requestConsent(Long doctorId, ConsentRequestDto req) {
        // recordId == null or 0 means "access to all sensitive records for this patient"
        // In that case we need the patientId from context — we use the first sensitive record we find
        // OR we create a general consent entry without a specific record
        Long recordId = req.getRecordId();

        // If no specific record, find the first sensitive record for any patient this doctor has treated
        if (recordId == null || recordId == 0) {
            // Generic request — find patient from doctor's appointments
            List<com.medvault.entity.Appointment> appts = appointmentRepository
                    .findByDoctorIdOrderByAppointmentDateAscAppointmentTimeAsc(doctorId);
            // For generic request we need patient context — find first sensitive record for recently treated patients
            for (com.medvault.entity.Appointment appt : appts) {
                List<MedicalRecord> sensitiveRecs = recordRepository
                        .findByPatientIdAndIsActiveTrueOrderByCreatedAtDesc(appt.getPatient().getId())
                        .stream().filter(r -> Boolean.TRUE.equals(r.getIsSensitive())).collect(java.util.stream.Collectors.toList());
                if (!sensitiveRecs.isEmpty()) {
                    recordId = sensitiveRecs.get(0).getId();
                    break;
                }
            }
            if (recordId == null || recordId == 0) {
                return ApiResponse.error("No sensitive records found for your patients.");
            }
        }

        final Long finalRecordId = recordId;
        MedicalRecord record = recordRepository.findById(finalRecordId)
                .orElseThrow(() -> new RuntimeException("Record not found: " + finalRecordId));

        if (!Boolean.TRUE.equals(record.getIsSensitive())) {
            return ApiResponse.error("This record is not marked sensitive — no consent required.");
        }

        if (consentRepository.existsByDoctorIdAndRecordIdAndStatus(doctorId, finalRecordId, "PENDING")) {
            return ApiResponse.error("A consent request is already pending for this record.");
        }

        User doctor  = getUser(doctorId);
        User patient = record.getPatient();

        ConsentRequest consent = ConsentRequest.builder()
                .doctor(doctor)
                .patient(patient)
                .record(record)
                .reason(req.getReason())
                .status("PENDING")
                .build();

        consentRepository.save(consent);
        log.info("Consent requested: doctor={}, record={}", doctorId, recordId);
        return ApiResponse.ok("Consent request sent to patient.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getApprovedRecords(Long doctorId, Long patientId) {
        // Get record IDs the doctor has approved consent for
        List<Long> approvedRecordIds = consentRepository
                .findByDoctorIdOrderByRequestedAtDesc(doctorId)
                .stream()
                .filter(c -> c.getPatient().getId().equals(patientId) && "APPROVED".equals(c.getStatus()))
                .map(c -> c.getRecord().getId())
                .collect(Collectors.toList());

        // Return patient's non-sensitive records + approved sensitive records
        return recordRepository
                .findByPatientIdAndIsActiveTrueOrderByCreatedAtDesc(patientId)
                .stream()
                .filter(r -> !Boolean.TRUE.equals(r.getIsSensitive()) || approvedRecordIds.contains(r.getId()))
                .map(this::toRecordResponse)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════
    // PATIENT — CONSENT RESPONSES
    // ═══════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<ConsentRequestResponse> getPendingConsentRequests(Long patientId) {
        return consentRepository
                .findByPatientIdAndStatusOrderByRequestedAtDesc(patientId, "PENDING")
                .stream().map(this::toConsentResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsentRequestResponse> getAllConsentRequests(Long patientId) {
        return consentRepository
                .findByPatientIdOrderByRequestedAtDesc(patientId)
                .stream().map(this::toConsentResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ApiResponse<String> respondToConsent(Long patientId, Long consentId, ConsentActionRequest req) {
        ConsentRequest consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new RuntimeException("Consent request not found: " + consentId));

        if (!consent.getPatient().getId().equals(patientId)) {
            return ApiResponse.error("This consent request is not for you.");
        }
        if (!"PENDING".equals(consent.getStatus())) {
            return ApiResponse.error("This request has already been responded to.");
        }

        String action = req.getAction().toUpperCase();
        if (!"APPROVE".equals(action) && !"DENY".equals(action)) {
            return ApiResponse.error("Action must be APPROVE or DENY.");
        }

        consent.setStatus("APPROVE".equals(action) ? "APPROVED" : "DENIED");
        consent.setRespondedAt(LocalDateTime.now());
        consentRepository.save(consent);

        log.info("Consent {}: patient={}, consentId={}", consent.getStatus(), patientId, consentId);
        return ApiResponse.ok("Consent request " + consent.getStatus().toLowerCase() + ".");
    }

    // ═══════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<ConsentRequestResponse> getDoctorConsentRequests(Long doctorId) {
        return consentRepository.findByDoctorIdOrderByRequestedAtDesc(doctorId)
                .stream().map(this::toConsentResponse).collect(Collectors.toList());
    }

    private MedicalRecordResponse toRecordResponse(MedicalRecord r) {
        return MedicalRecordResponse.builder()
                .id(r.getId())
                .patientId(r.getPatient().getId())
                .category(r.getCategory())
                .title(r.getTitle())
                .description(r.getDescription())
                .fileName(r.getFileName())
                .fileType(r.getFileType())
                .fileUrl(r.getFilePath())
                .habitValue(r.getHabitValue())
                .isSensitive(r.getIsSensitive())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toString() : "")
                .build();
    }

    private ConsentRequestResponse toConsentResponse(ConsentRequest c) {
        return ConsentRequestResponse.builder()
                .id(c.getId())
                .doctorId(c.getDoctor().getId())
                .doctorName(getPersonName(c.getDoctor().getId()))
                .patientId(c.getPatient().getId())
                .patientName(getPersonName(c.getPatient().getId()))
                .recordId(c.getRecord().getId())
                .recordTitle(c.getRecord().getTitle())
                .recordCategory(c.getRecord().getCategory())
                .status(c.getStatus())
                .reason(c.getReason())
                .requestedAt(c.getRequestedAt() != null ? c.getRequestedAt().toString() : "")
                .respondedAt(c.getRespondedAt() != null ? c.getRespondedAt().toString() : "")
                .build();
    }

    private String getPersonName(Long userId) {
        return personalDetailsRepository.findByUserId(userId)
                .map(pd -> (pd.getFirstName() + " " + pd.getLastName()).trim())
                .orElse("Unknown");
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    private String saveFile(MultipartFile file, String subDir) {
        try {
            Path dir = Paths.get(uploadDir, subDir);
            Files.createDirectories(dir);
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return subDir + "/" + filename;  // relative path only
        } catch (IOException e) {
            log.error("File save failed: {}", e.getMessage());
            return null;
        }
    }
}