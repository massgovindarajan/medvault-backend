package com.medvault.controller;


import com.medvault.dto.request.PatientDTO;
import com.medvault.entity.Patient;
import com.medvault.repository.PatientRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientRepository patientRepository;

    // ── Register ──────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody PatientDTO dto) {

        if (patientRepository.existsByEmail(dto.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Email already registered.");
        }

        Patient patient = Patient.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .addressLine1(dto.getAddressLine1())
                .city(dto.getCity())
                .state(dto.getState())
                .pincode(dto.getPincode())
                .bloodGroup(dto.getBloodGroup())
                .allergies(dto.getAllergies())
                .medicalHistory(dto.getMedicalHistory())
                .emergencyContactName(dto.getEmergencyContactName())
                .emergencyContactPhone(dto.getEmergencyContactPhone())
                .passwordHash(dto.getPassword()) // hash this with BCrypt in service later
                .build();

        Patient saved = patientRepository.save(patient);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ── Get All ───────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<Patient>> getAllPatients() {
        return ResponseEntity.ok(patientRepository.findAll());
    }

    // ── Get By ID ─────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getPatientById(@PathVariable Long id) {
        Optional<Patient> patient = patientRepository.findById(id);
        return patient.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Update ────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientDTO dto) {

        return patientRepository.findById(id).map(existing -> {
            existing.setFirstName(dto.getFirstName());
            existing.setLastName(dto.getLastName());
            existing.setPhone(dto.getPhone());
            existing.setDateOfBirth(dto.getDateOfBirth());
            existing.setGender(dto.getGender());
            existing.setAddressLine1(dto.getAddressLine1());
            existing.setCity(dto.getCity());
            existing.setState(dto.getState());
            existing.setPincode(dto.getPincode());
            existing.setBloodGroup(dto.getBloodGroup());
            existing.setAllergies(dto.getAllergies());
            existing.setMedicalHistory(dto.getMedicalHistory());
            existing.setEmergencyContactName(dto.getEmergencyContactName());
            existing.setEmergencyContactPhone(dto.getEmergencyContactPhone());
            return ResponseEntity.ok(patientRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Delete ────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePatient(@PathVariable Long id) {
        if (!patientRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        patientRepository.deleteById(id);
        return ResponseEntity.ok("Patient deleted successfully.");
    }
}