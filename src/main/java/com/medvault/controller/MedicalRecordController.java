package com.medvault.controller;

import com.medvault.dto.request.ConsentActionRequest;
import com.medvault.dto.request.ConsentRequestDto;
import com.medvault.dto.request.MedicalRecordRequest;
import com.medvault.dto.response.ApiResponse;
import com.medvault.dto.response.ConsentRequestResponse;
import com.medvault.dto.response.MedicalRecordResponse;
import com.medvault.repository.UserRepository;
import com.medvault.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService recordService;
    private final UserRepository       userRepository;

    // ── Patient: upload record ──────────────────────────
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> upload(
            @RequestPart("data")  MedicalRecordRequest req,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserDetails principal) {

        return ResponseEntity.ok(recordService.uploadRecord(getUserId(principal), req, file));
    }

    // ── Patient: get all my records ─────────────────────
    @GetMapping("/my")
    public ResponseEntity<List<MedicalRecordResponse>> myRecords(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(recordService.getMyRecords(getUserId(principal)));
    }

    // ── Patient: get my records by category ────────────
    @GetMapping("/my/{category}")
    public ResponseEntity<List<MedicalRecordResponse>> myRecordsByCategory(
            @PathVariable String category,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(recordService.getMyRecordsByCategory(getUserId(principal), category));
    }

    // ── Patient: soft-delete record ─────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(recordService.deleteRecord(getUserId(principal), id));
    }

    // ── Doctor: request consent for sensitive record ────
    @PostMapping("/consent/request")
    public ResponseEntity<ApiResponse<String>> requestConsent(
            @Valid @RequestBody ConsentRequestDto req,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(recordService.requestConsent(getUserId(principal), req));
    }

    // ── Doctor: view approved records for a patient ────
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalRecordResponse>> patientRecords(
            @PathVariable Long patientId,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(recordService.getApprovedRecords(getUserId(principal), patientId));
    }

    // ── Patient: pending consent requests ──────────────
    @GetMapping("/consent/pending")
    public ResponseEntity<List<ConsentRequestResponse>> pendingConsents(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(recordService.getPendingConsentRequests(getUserId(principal)));
    }

    // ── Patient: all consent requests ──────────────────
    @GetMapping("/consent/all")
    public ResponseEntity<List<ConsentRequestResponse>> allConsents(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(recordService.getAllConsentRequests(getUserId(principal)));
    }

    // ── Doctor: view my consent requests ─────────────────
    @GetMapping("/consent/doctor")
    public ResponseEntity<List<ConsentRequestResponse>> doctorConsents(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(recordService.getDoctorConsentRequests(getUserId(principal)));
    }

    // ── Patient: respond to consent ────────────────────
    @PutMapping("/consent/{id}/respond")
    public ResponseEntity<ApiResponse<String>> respondConsent(
            @PathVariable Long id,
            @RequestBody ConsentActionRequest req,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(recordService.respondToConsent(getUserId(principal), id, req));
    }

    private Long getUserId(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
    }
}