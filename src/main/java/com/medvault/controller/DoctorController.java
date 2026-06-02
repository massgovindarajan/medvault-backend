package com.medvault.controller;

import com.medvault.dto.response.DoctorResponse;
import com.medvault.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final AppointmentService appointmentService;

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> all() {
        return ResponseEntity.ok(appointmentService.getAllDoctors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> one(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getDoctorById(id));
    }
}