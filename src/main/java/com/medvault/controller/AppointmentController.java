//package com.medvault.controller;
//
//import com.medvault.dto.request.AppointmentRequest;
//import com.medvault.dto.request.RatingRequest;
//import com.medvault.dto.request.RejectRequest;
//import com.medvault.dto.response.*;
//import com.medvault.repository.UserRepository;
//import com.medvault.service.AppointmentService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/appointments")
//@RequiredArgsConstructor
//public class AppointmentController {
//
//    private final AppointmentService appointmentService;
//    private final UserRepository     userRepository;
//
//    // ── Book appointment (patient) ─────────────────────────
//    @PostMapping(value = "/book", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<ApiResponse<AppointmentResponse>> book(
//            @RequestPart("data")       AppointmentRequest req,
//            @RequestPart(value = "prescription", required = false) MultipartFile file) {
//
//        return ResponseEntity.ok(appointmentService.bookAppointment(req, file));
//    }
//
//    // ── Patient: my appointments ───────────────────────────
//    @GetMapping("/my")
//    public ResponseEntity<List<AppointmentResponse>> myAppointments(
//            @AuthenticationPrincipal UserDetails principal) {
//
//        Long userId = getUserId(principal);
//        return ResponseEntity.ok(appointmentService.getMyAppointments(userId));
//    }
//
//    // ── Doctor: full schedule ──────────────────────────────
//    @GetMapping("/doctor")
//    public ResponseEntity<List<AppointmentResponse>> doctorAppointments(
//            @AuthenticationPrincipal UserDetails principal) {
//
//        Long doctorId = getUserId(principal);
//        return ResponseEntity.ok(appointmentService.getDoctorAppointments(doctorId));
//    }
//
//    // ── Doctor: pending only ───────────────────────────────
//    @GetMapping("/pending")
//    public ResponseEntity<List<AppointmentResponse>> pendingAppointments(
//            @AuthenticationPrincipal UserDetails principal) {
//
//        Long doctorId = getUserId(principal);
//        return ResponseEntity.ok(appointmentService.getPendingAppointments(doctorId));
//    }
//
//    // ── Receptionist / Admin: all appointments ─────────────
//    @GetMapping("/all")
//    public ResponseEntity<List<AppointmentResponse>> allAppointments() {
//        return ResponseEntity.ok(appointmentService.getAllAppointments());
//    }
//
//    // ── Approve ────────────────────────────────────────────
//    @PutMapping("/{id}/approve")
//    public ResponseEntity<ApiResponse<String>> approve(@PathVariable Long id) {
//        return ResponseEntity.ok(appointmentService.approveAppointment(id));
//    }
//
//    // ── Reject ─────────────────────────────────────────────
//    @PutMapping("/{id}/reject")
//    public ResponseEntity<ApiResponse<String>> reject(
//            @PathVariable Long id,
//            @RequestBody RejectRequest req) {
//        return ResponseEntity.ok(appointmentService.rejectAppointment(id, req));
//    }
//
//    // ── Complete ───────────────────────────────────────────
//    @PutMapping("/{id}/complete")
//    public ResponseEntity<ApiResponse<String>> complete(@PathVariable Long id) {
//        return ResponseEntity.ok(appointmentService.completeAppointment(id));
//    }
//
//    // ── Helper ────────────────────────────────────────────
//    private Long getUserId(UserDetails principal) {
//        return userRepository.findByEmail(principal.getUsername())
//                .orElseThrow(() -> new RuntimeException("User not found"))
//                .getId();
//    }
//}

package com.medvault.controller;

import com.medvault.dto.request.AppointmentRequest;
import com.medvault.dto.request.RatingRequest;
import com.medvault.dto.request.RejectRequest;
import com.medvault.dto.response.*;
import com.medvault.repository.UserRepository;
import com.medvault.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserRepository     userRepository;

    // Book appointment (patient)
    @PostMapping(value = "/book", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AppointmentResponse>> book(
            @RequestPart("data") AppointmentRequest req,
            @RequestPart(value = "prescription", required = false) MultipartFile file) {
        return ResponseEntity.ok(appointmentService.bookAppointment(req, file));
    }

    // Patient: my appointments
    @GetMapping("/my")
    public ResponseEntity<List<AppointmentResponse>> myAppointments(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(appointmentService.getMyAppointments(getUserId(principal)));
    }

    // Doctor: full schedule
    @GetMapping("/doctor")
    public ResponseEntity<List<AppointmentResponse>> doctorAppointments(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(appointmentService.getDoctorAppointments(getUserId(principal)));
    }

    // Doctor: pending only
    @GetMapping("/pending")
    public ResponseEntity<List<AppointmentResponse>> pendingAppointments(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(appointmentService.getPendingAppointments(getUserId(principal)));
    }

    // Receptionist / Admin: all appointments
    @GetMapping("/all")
    public ResponseEntity<List<AppointmentResponse>> allAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    // Approve
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<String>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.approveAppointment(id));
    }

    // Reject
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<String>> reject(
            @PathVariable Long id, @RequestBody RejectRequest req) {
        return ResponseEntity.ok(appointmentService.rejectAppointment(id, req));
    }

    // Complete
    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<String>> complete(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.completeAppointment(id));
    }

    /**
     * Patient cancels their own appointment.
     * Time-based refund policy applied automatically:
     *   > 24 hrs  → FULL refund
     *   6-24 hrs  → PARTIAL refund (50%)
     *   < 6 hrs   → NO refund
     * Slot is freed so it becomes bookable again.
     * Doctor + Patient are notified.
     *
     * PUT /api/appointments/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<CancellationResponse>> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        Long patientId = getUserId(principal);
        return ResponseEntity.ok(appointmentService.cancelAppointment(id, patientId));
    }
    @PostMapping("/walk-in")
    public ResponseEntity<ApiResponse<AppointmentResponse>> walkIn(
            @RequestBody AppointmentRequest req) {
        return ResponseEntity.ok(appointmentService.bookAppointment(req, null));
    }

    // Helper
    private Long getUserId(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}