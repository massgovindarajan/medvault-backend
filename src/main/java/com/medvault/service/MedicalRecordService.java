package com.medvault.service;

import com.medvault.dto.request.ConsentActionRequest;
import com.medvault.dto.request.ConsentRequestDto;
import com.medvault.dto.request.MedicalRecordRequest;
import com.medvault.dto.response.ApiResponse;
import com.medvault.dto.response.ConsentRequestResponse;
import com.medvault.dto.response.MedicalRecordResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MedicalRecordService {

    // Patient: upload / manage own records
    ApiResponse<MedicalRecordResponse> uploadRecord(Long patientId, MedicalRecordRequest req, MultipartFile file);
    List<MedicalRecordResponse>        getMyRecords(Long patientId);
    List<MedicalRecordResponse>        getMyRecordsByCategory(Long patientId, String category);
    ApiResponse<String>                deleteRecord(Long patientId, Long recordId);

    // Doctor: request consent + view approved records
    ApiResponse<String>                requestConsent(Long doctorId, ConsentRequestDto req);
    List<MedicalRecordResponse>        getApprovedRecords(Long doctorId, Long patientId);

    // Doctor: view own consent requests history
    List<ConsentRequestResponse>       getDoctorConsentRequests(Long doctorId);

    // Patient: view + act on consent requests
    List<ConsentRequestResponse>       getPendingConsentRequests(Long patientId);
    List<ConsentRequestResponse>       getAllConsentRequests(Long patientId);
    ApiResponse<String>                respondToConsent(Long patientId, Long consentId, ConsentActionRequest req);
}