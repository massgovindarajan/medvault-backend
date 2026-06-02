//package com.medvault.controller;
//
//import com.medvault.dto.request.DispenseRequest;
//import com.medvault.dto.request.MedicineInventoryRequest;
//import com.medvault.dto.request.PrescriptionRequest;
//import com.medvault.dto.response.ApiResponse;
//import com.medvault.dto.response.MedicineInventoryResponse;
//import com.medvault.dto.response.PrescriptionResponse;
//import com.medvault.repository.UserRepository;
//import com.medvault.service.PharmacyService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//
//@RestController
//@RequestMapping("/api/pharmacy")
//@RequiredArgsConstructor
//public class PharmacyController {
//
//    private final PharmacyService pharmacyService;
//    private final UserRepository  userRepository;
//
//
//    // PRESCRIPTIONS — Doctor writes
//
//
//    @PostMapping("/prescriptions")
//    public ResponseEntity<ApiResponse<PrescriptionResponse>> writePrescription(
//            @RequestBody PrescriptionRequest req,
//            @AuthenticationPrincipal UserDetails principal) {
//        Long doctorId = getUserId(principal);
//        return ResponseEntity.ok(pharmacyService.writePrescription(doctorId, req));
//    }
//
//
//    @GetMapping("/prescriptions/my")
//    public ResponseEntity<List<PrescriptionResponse>> myPrescriptions(
//            @AuthenticationPrincipal UserDetails principal) {
//        Long doctorId = getUserId(principal);
//        return ResponseEntity.ok(pharmacyService.getDoctorPrescriptions(doctorId));
//    }
//
//
//    // PRESCRIPTIONS — Pharmacist reads & dispenses
//
//
//    @GetMapping("/prescriptions/pending")
//    public ResponseEntity<List<PrescriptionResponse>> pending() {
//        return ResponseEntity.ok(pharmacyService.getPendingPrescriptions());
//    }
//
//
//    @GetMapping("/prescriptions/all")
//    public ResponseEntity<List<PrescriptionResponse>> all() {
//        return ResponseEntity.ok(pharmacyService.getAllPrescriptions());
//    }
//
// 
//    @GetMapping("/prescriptions/{id}")
//    public ResponseEntity<PrescriptionResponse> getOne(@PathVariable Long id) {
//        return ResponseEntity.ok(pharmacyService.getPrescriptionById(id));
//    }
//
//  
//    @PutMapping("/prescriptions/{id}/dispense")
//    public ResponseEntity<ApiResponse<PrescriptionResponse>> dispense(
//            @PathVariable Long id,
//            @RequestBody(required = false) DispenseRequest req,
//            @AuthenticationPrincipal UserDetails principal) {
//        Long pharmacistId = getUserId(principal);
//        if (req == null) req = new DispenseRequest();
//        return ResponseEntity.ok(pharmacyService.dispensePrescription(id, pharmacistId, req));
//    }
//
// 
//    // PRESCRIPTIONS — Patient views own
//  
//    @GetMapping("/prescriptions/patient")
//    public ResponseEntity<List<PrescriptionResponse>> patientPrescriptions(
//            @AuthenticationPrincipal UserDetails principal) {
//        Long patientId = getUserId(principal);
//        return ResponseEntity.ok(pharmacyService.getPatientPrescriptions(patientId));
//    }
//
//  
//    // MEDICINE INVENTORY
//   
//    @GetMapping("/inventory")
//    public ResponseEntity<List<MedicineInventoryResponse>> getInventory() {
//        return ResponseEntity.ok(pharmacyService.getAllMedicines());
//    }
//
//  
//    @GetMapping("/inventory/low-stock")
//    public ResponseEntity<List<MedicineInventoryResponse>> lowStock() {
//        return ResponseEntity.ok(pharmacyService.getLowStockMedicines());
//    }
//
//   
//    @PostMapping("/inventory")
//    public ResponseEntity<ApiResponse<MedicineInventoryResponse>> addMedicine(
//            @RequestBody MedicineInventoryRequest req) {
//        return ResponseEntity.ok(pharmacyService.addMedicine(req));
//    }
//
//   
//    @PutMapping("/inventory/{id}")
//    public ResponseEntity<ApiResponse<MedicineInventoryResponse>> updateMedicine(
//            @PathVariable Long id,
//            @RequestBody MedicineInventoryRequest req) {
//        return ResponseEntity.ok(pharmacyService.updateMedicine(id, req));
//    }
//
//    @DeleteMapping("/inventory/{id}")
//    public ResponseEntity<ApiResponse<String>> deleteMedicine(@PathVariable Long id) {
//        return ResponseEntity.ok(pharmacyService.deleteMedicine(id));
//    }
//
//   
//    // HELPER
//   
//    private Long getUserId(UserDetails principal) {
//        return userRepository.findByEmail(principal.getUsername())
//                .orElseThrow(() -> new RuntimeException("User not found"))
//                .getId();
//    }
//}

package com.medvault.controller;

import com.medvault.dto.request.DispenseRequest;
import com.medvault.dto.request.MedicineInventoryRequest;
import com.medvault.dto.request.PrescriptionRequest;
import com.medvault.dto.response.ApiResponse;
import com.medvault.dto.response.MedicineInventoryResponse;
import com.medvault.dto.response.PrescriptionResponse;
import com.medvault.repository.UserRepository;
import com.medvault.service.PharmacyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/pharmacy")
@RequiredArgsConstructor
public class PharmacyController {

    private final PharmacyService pharmacyService;
    private final UserRepository  userRepository;

    // ── Doctor writes prescription ──────────────────────────────────
    @PostMapping("/prescriptions")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> writePrescription(
            @RequestBody PrescriptionRequest req,
            @AuthenticationPrincipal UserDetails principal) {
        try {
            Long doctorId = getUserId(principal);
            return ResponseEntity.ok(pharmacyService.writePrescription(doctorId, req));
        } catch (Exception e) {
            log.error("Failed to write prescription: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to write prescription: " + e.getMessage()));
        }
    }

    // ── Doctor views own prescriptions ──────────────────────────────
    @GetMapping("/prescriptions/my")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> myPrescriptions(
            @AuthenticationPrincipal UserDetails principal) {
        try {
            Long doctorId = getUserId(principal);
            List<PrescriptionResponse> data = pharmacyService.getDoctorPrescriptions(doctorId);
            return ResponseEntity.ok(ApiResponse.ok("Prescriptions fetched.", data));
        } catch (Exception e) {
            log.error("Failed to fetch doctor prescriptions: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to fetch prescriptions."));
        }
    }

    // ── Patient views own prescriptions ────────────────────────────
    @GetMapping("/prescriptions/patient")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> patientPrescriptions(
            @AuthenticationPrincipal UserDetails principal) {
        try {
            Long patientId = getUserId(principal);
            List<PrescriptionResponse> data = pharmacyService.getPatientPrescriptions(patientId);
            return ResponseEntity.ok(ApiResponse.ok("Prescriptions fetched.", data));
        } catch (Exception e) {
            log.error("Failed to fetch patient prescriptions: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to fetch my prescriptions."));
        }
    }

    // ── Pharmacist — pending ────────────────────────────────────────
    @GetMapping("/prescriptions/pending")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> pending() {
        try {
            List<PrescriptionResponse> data = pharmacyService.getPendingPrescriptions();
            return ResponseEntity.ok(ApiResponse.ok("Pending prescriptions fetched.", data));
        } catch (Exception e) {
            log.error("Failed to fetch pending prescriptions: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to fetch pending prescriptions."));
        }
    }

    // ── Pharmacist — all ───────────────────────────────────────────
    @GetMapping("/prescriptions/all")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> all() {
        try {
            List<PrescriptionResponse> data = pharmacyService.getAllPrescriptions();
            return ResponseEntity.ok(ApiResponse.ok("All prescriptions fetched.", data));
        } catch (Exception e) {
            log.error("Failed to fetch all prescriptions: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to fetch all prescriptions."));
        }
    }

    // ── Get single prescription ────────────────────────────────────
    @GetMapping("/prescriptions/{id}")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> getOne(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Prescription fetched.", pharmacyService.getPrescriptionById(id)));
        } catch (Exception e) {
            log.error("Failed to fetch prescription {}: {}", id, e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Prescription not found."));
        }
    }

    // ── Pharmacist dispenses ───────────────────────────────────────
    @PutMapping("/prescriptions/{id}/dispense")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> dispense(
            @PathVariable Long id,
            @RequestBody(required = false) DispenseRequest req,
            @AuthenticationPrincipal UserDetails principal) {
        try {
            Long pharmacistId = getUserId(principal);
            if (req == null) req = new DispenseRequest();
            return ResponseEntity.ok(pharmacyService.dispensePrescription(id, pharmacistId, req));
        } catch (Exception e) {
            log.error("Failed to dispense prescription {}: {}", id, e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to dispense prescription."));
        }
    }

    // ── Medicine Inventory ─────────────────────────────────────────
    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<List<MedicineInventoryResponse>>> getInventory() {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Inventory fetched.", pharmacyService.getAllMedicines()));
        } catch (Exception e) {
            log.error("Failed to fetch inventory: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to fetch inventory."));
        }
    }

    @GetMapping("/inventory/low-stock")
    public ResponseEntity<ApiResponse<List<MedicineInventoryResponse>>> lowStock() {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Low stock fetched.", pharmacyService.getLowStockMedicines()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Failed to fetch low stock medicines."));
        }
    }

    @PostMapping("/inventory")
    public ResponseEntity<ApiResponse<MedicineInventoryResponse>> addMedicine(
            @RequestBody MedicineInventoryRequest req) {
        try {
            return ResponseEntity.ok(pharmacyService.addMedicine(req));
        } catch (Exception e) {
            log.error("Failed to add medicine: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to add medicine."));
        }
    }

    @PutMapping("/inventory/{id}")
    public ResponseEntity<ApiResponse<MedicineInventoryResponse>> updateMedicine(
            @PathVariable Long id,
            @RequestBody MedicineInventoryRequest req) {
        try {
            return ResponseEntity.ok(pharmacyService.updateMedicine(id, req));
        } catch (Exception e) {
            log.error("Failed to update medicine {}: {}", id, e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to update medicine."));
        }
    }

    @DeleteMapping("/inventory/{id}")
    public ResponseEntity<ApiResponse<String>> deleteMedicine(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(pharmacyService.deleteMedicine(id));
        } catch (Exception e) {
            log.error("Failed to delete medicine {}: {}", id, e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to delete medicine."));
        }
    }

    // ── Helper ─────────────────────────────────────────────────────
    private Long getUserId(UserDetails principal) {
        if (principal == null) throw new RuntimeException("Not authenticated");
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + principal.getUsername()))
                .getId();
    }
}