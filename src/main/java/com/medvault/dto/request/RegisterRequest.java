package com.medvault.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {

    // ── Step 1: Account ──────────────────────────────
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @NotBlank(message = "Role is required")
    private String role;            // plain String — frontend sends "DOCTOR", "PATIENT" etc.

    // ── Step 2: Personal Details ──────────────────────
    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;

    @NotNull(message = "Date of birth is required")
    private LocalDate dob;

    private String gender;          // plain String — "MALE", "FEMALE", "OTHER"

    // ── Step 3: Address ───────────────────────────────
    @NotBlank(message = "Street is required")
    private String street;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be 6 digits")
    private String pincode;

    private String country;

    // ── Step 4: Identity Document ─────────────────────
    @NotBlank(message = "Document type is required")
    private String documentType;    // plain String — "AADHAAR", "PAN", etc.

    @NotBlank(message = "Document number is required")
    private String documentNumber;

    // ── Step 5: Education (non-PATIENT) ──────────────
    private String degree;
    private String institution;
    private Integer yearOfCompletion;

    // ── Step 6: Work Experience (non-PATIENT) ─────────
    private String organizationName;
    private String roleTitle;
    private LocalDate startDate;
    private LocalDate endDate;

    // ── Step 7: Medical Certificate (DOCTOR) ──────────
    private String licenseNumber;
    private String registrationCouncil;
    private LocalDate validFrom;
    private LocalDate validTo;

    // ── Step 8: Patient Health Info ───────────────────
    private String bloodGroup;
    private String allergies;
    private String chronicConditions;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String insuranceProvider;
    private String insurancePolicyNo;
}