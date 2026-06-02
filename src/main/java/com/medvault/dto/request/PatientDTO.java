package com.medvault.dto.request;

import com.medvault.entity.Patient.BloodGroup;
import com.medvault.entity.Patient.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PatientDTO {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email @NotBlank
    private String email;

    @NotBlank
    private String phone;

    @NotNull
    private LocalDate dateOfBirth;

    @NotNull
    private Gender gender;

    private String addressLine1;
    private String city;
    private String state;
    private String pincode;

    private BloodGroup bloodGroup;
    private String allergies;
    private String medicalHistory;

    private String emergencyContactName;
    private String emergencyContactPhone;

    @NotBlank @Size(min = 8)
    private String password;
}
