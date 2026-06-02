package com.medvault.controller;

import com.medvault.dto.request.RatingRequest;
import com.medvault.dto.response.ApiResponse;
import com.medvault.dto.response.RatingResponse;
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
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final AppointmentService appointmentService;
    private final UserRepository     userRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> submit(
            @Valid @RequestBody RatingRequest req,
            @AuthenticationPrincipal UserDetails principal) {

        Long patientId = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
        return ResponseEntity.ok(appointmentService.submitRating(patientId, req));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<RatingResponse>> doctorRatings(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getDoctorRatings(doctorId));
    }
}