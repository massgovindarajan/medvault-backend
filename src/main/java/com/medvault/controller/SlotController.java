package com.medvault.controller;

import com.medvault.dto.request.SlotRequest;
import com.medvault.dto.response.ApiResponse;
import com.medvault.dto.response.SlotResponse;
import com.medvault.repository.UserRepository;
import com.medvault.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class SlotController {

    private final AppointmentService appointmentService;
    private final UserRepository     userRepository;

    // GET /api/slots  (all)  OR  /api/slots?doctorId=&date=
    @GetMapping
    public ResponseEntity<List<SlotResponse>> getSlots(
            @RequestParam(required = false) Long   doctorId,
            @RequestParam(required = false) String date) {

        if (doctorId != null && date != null) {
            return ResponseEntity.ok(appointmentService.getSlotsByDoctorAndDate(doctorId, date));
        }
        return ResponseEntity.ok(appointmentService.getAllSlots());
    }

    // POST /api/slots
    @PostMapping
    public ResponseEntity<ApiResponse<SlotResponse>> create(
            @Valid @RequestBody SlotRequest req,
            @AuthenticationPrincipal UserDetails principal) {

        Long adminId = getUserId(principal);
        SlotResponse slot = appointmentService.createSlot(adminId, req);
        return ResponseEntity.ok(ApiResponse.ok("Slot created.", slot));
    }

    // PUT /api/slots/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SlotResponse>> update(
            @PathVariable Long id,
            @RequestBody SlotRequest req) {

        SlotResponse slot = appointmentService.updateSlot(id, req);
        return ResponseEntity.ok(ApiResponse.ok("Slot updated.", slot));
    }

    // DELETE /api/slots/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {
        appointmentService.deleteSlot(id);
        return ResponseEntity.ok(ApiResponse.ok("Slot deleted."));
    }

    private Long getUserId(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}