package com.medvault.service.impl;

import com.medvault.dto.request.LoginRequest;
import com.medvault.dto.request.RegisterRequest;
import com.medvault.dto.request.ResetPasswordRequest;
import com.medvault.dto.response.ApiResponse;
import com.medvault.dto.response.LoginResponse;
import com.medvault.entity.Address;
import com.medvault.entity.EducationDetails;
import com.medvault.entity.IdentityDocument;
import com.medvault.entity.MedicalCertificate;
import com.medvault.entity.PersonalDetails;
import com.medvault.entity.User;
import com.medvault.entity.WorkExperience;
import com.medvault.repository.AddressRepository;
import com.medvault.repository.EducationDetailsRepository;
import com.medvault.repository.IdentityDocumentRepository;
import com.medvault.repository.MedicalCertificateRepository;
import com.medvault.repository.PersonalDetailsRepository;
import com.medvault.repository.UserRepository;
import com.medvault.repository.WorkExperienceRepository;
import com.medvault.security.JwtUtil;
import com.medvault.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository             userRepository;
    private final PersonalDetailsRepository  personalDetailsRepository;
    private final AddressRepository          addressRepository;
    private final EducationDetailsRepository educationDetailsRepository;
    private final WorkExperienceRepository   workExperienceRepository;
    private final IdentityDocumentRepository identityDocumentRepository;
    private final MedicalCertificateRepository medicalCertificateRepository;

    private final PasswordEncoder       passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil               jwtUtil;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // ─────────────────────────────────────────────────
    //  REGISTER
    // ─────────────────────────────────────────────────
    @Override
    @Transactional
    public ApiResponse<String> register(RegisterRequest req,
                                         MultipartFile profilePhoto,
                                         MultipartFile idCard,
                                         MultipartFile degreeCert,
                                         MultipartFile licenceCard) {

        if (userRepository.existsByEmail(req.getEmail())) {
            return ApiResponse.error("Email already registered.");
        }

        if ("DOCTOR".equals(req.getRole())) {
            if (req.getLicenseNumber() == null || req.getLicenseNumber().isBlank()) {
                return ApiResponse.error("Medical license number is mandatory for Doctors.");
            }
        }

        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode("PENDING_" + System.currentTimeMillis()))
                .role(req.getRole())
                .status("PENDING")
                .passwordResetRequired(true)
                .l1Approved(false)
                .l2Approved(false)
                .build();
        user = userRepository.save(user);

        // Save uploaded files
        String profilePhotoPath = saveFile(profilePhoto, "profiles/" + user.getId());
        String idCardPath        = saveFile(idCard,       "documents/" + user.getId());
        String degreeCertPath    = saveFile(degreeCert,   "documents/" + user.getId());
        String licenceCardPath   = saveFile(licenceCard,  "documents/" + user.getId());

        // Personal Details
        PersonalDetails pd = PersonalDetails.builder()
                .user(user)
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .dob(req.getDob())
                .gender(req.getGender())
                .phone(req.getPhone())
                .profilePhotoPath(profilePhotoPath)
                .build();
        personalDetailsRepository.save(pd);

        // Address
        Address addr = Address.builder()
                .user(user)
                .street(req.getStreet())
                .city(req.getCity())
                .state(req.getState())
                .pincode(req.getPincode())
                .country(req.getCountry() != null ? req.getCountry() : "India")
                .build();
        addressRepository.save(addr);

        // Identity Document
        IdentityDocument doc = IdentityDocument.builder()
                .user(user)
                .documentType(req.getDocumentType())
                .documentNumber(req.getDocumentNumber())
                .filePath(idCardPath)
                .verifiedStatus("PENDING")
                .build();
        identityDocumentRepository.save(doc);

        // Education & Work (non-PATIENT)
        if (!"PATIENT".equals(req.getRole())) {
            if (req.getDegree() != null && !req.getDegree().isBlank()) {
                EducationDetails edu = EducationDetails.builder()
                        .user(user)
                        .degree(req.getDegree())
                        .institution(req.getInstitution())
                        .yearOfCompletion(req.getYearOfCompletion())
                        .certificatePath(degreeCertPath)
                        .build();
                educationDetailsRepository.save(edu);
            }
            if (req.getOrganizationName() != null && !req.getOrganizationName().isBlank()) {
                WorkExperience work = WorkExperience.builder()
                        .user(user)
                        .organizationName(req.getOrganizationName())
                        .role(req.getRoleTitle())
                        .startDate(req.getStartDate())
                        .endDate(req.getEndDate())
                        .build();
                workExperienceRepository.save(work);
            }
        }

        // Medical Certificate (DOCTOR or RECEPTIONIST)
        if ("DOCTOR".equals(req.getRole()) || "RECEPTIONIST".equals(req.getRole())) {
            if (req.getLicenseNumber() != null && !req.getLicenseNumber().isBlank()) {
                MedicalCertificate cert = MedicalCertificate.builder()
                        .user(user)
                        .licenseNumber(req.getLicenseNumber())
                        .certificateFilePath(licenceCardPath)
                        .registrationCouncil(req.getRegistrationCouncil())
                        .validFrom(req.getValidFrom())
                        .validTo(req.getValidTo())
                        .verificationStatus("PENDING")
                        .build();
                medicalCertificateRepository.save(cert);
            }
        }

        log.info("New registration submitted: {} ({})", req.getEmail(), req.getRole());
        return ApiResponse.ok(
            "Registration submitted! Your account is pending admin approval. " +
            "You will receive credentials once both L1 and L2 admins verify your details."
        );
    }

    // ─────────────────────────────────────────────────
    //  LOGIN
    // ─────────────────────────────────────────────────
    @Override
    public LoginResponse login(LoginRequest req) {

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        if ("PENDING".equals(user.getStatus())) {
            return LoginResponse.builder()
                    .message("Your account is pending admin approval. Please wait.")
                    .status("PENDING")
                    .build();
        }

        if ("REJECTED".equals(user.getStatus())) {
            return LoginResponse.builder()
                    .message("Your account has been rejected. Reason: " + user.getRejectionReason())
                    .status("REJECTED")
                    .build();
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .passwordResetRequired(Boolean.TRUE.equals(user.getPasswordResetRequired()))
                .adminLevel(user.getAdminLevel())   // ✅ send admin level to frontend
                .message(Boolean.TRUE.equals(user.getPasswordResetRequired())
                        ? "First login detected. Please reset your password."
                        : "Login successful.")
                .build();
    }

    // ─────────────────────────────────────────────────
    //  RESET PASSWORD
    // ─────────────────────────────────────────────────
    @Override
    @Transactional
    public ApiResponse<String> resetPassword(Long userId, ResetPasswordRequest req) {

        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            return ApiResponse.error("Passwords do not match.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setPasswordResetRequired(false);
        userRepository.save(user);

        return ApiResponse.ok("Password reset successfully. You can now use your new password.");
    }

    // ─────────────────────────────────────────────────
    //  GENERATE TEMP PASSWORD (called by AdminServiceImpl on L2 approval)
    //  Format: FirstName + LastName + DOB + Last3DigitsOfPhone
    // ─────────────────────────────────────────────────
    public String generateTempPassword(User user) {
        PersonalDetails pd = personalDetailsRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Personal details not found"));

        String firstName   = pd.getFirstName() != null ? pd.getFirstName() : "";
        String lastName    = pd.getLastName()  != null ? pd.getLastName()  : "";
        String dob         = pd.getDob()       != null ? pd.getDob().toString() : "";
        String phone       = pd.getPhone()     != null ? pd.getPhone() : "000";
        String last3       = phone.length() >= 3 ? phone.substring(phone.length() - 3) : phone;

        return firstName + lastName + dob + last3;
    }

    // ─────────────────────────────────────────────────
    //  HELPER: Save uploaded file to disk
    // ─────────────────────────────────────────────────
    private String saveFile(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            log.debug("saveFile: skipped — file is null or empty for subDir={}", subDir);
            return null;
        }
        try {
            String ext      = "";
            String original = file.getOriginalFilename();
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
            }
            String filename = UUID.randomUUID().toString() + ext;

            // Use absolute path to avoid CWD ambiguity
            Path baseDir = Paths.get(uploadDir).isAbsolute()
                    ? Paths.get(uploadDir)
                    : Paths.get(System.getProperty("user.dir"), uploadDir);

            Path dirPath  = baseDir.resolve(subDir);
            Files.createDirectories(dirPath);
            Path dest = dirPath.resolve(filename);

            // Use Files.copy instead of transferTo for reliability
            try (java.io.InputStream in = file.getInputStream()) {
                Files.copy(in, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            String relativePath = subDir + "/" + filename;
            log.info("saveFile: saved {} -> {}", original, dest.toAbsolutePath());
            return relativePath;
        } catch (IOException e) {
            log.error("saveFile: FAILED for subDir={} error={}", subDir, e.getMessage(), e);
            return null;
        }
    }
}