package com.medvault.controller;

import com.medvault.dto.request.LoginRequest;
import com.medvault.dto.request.RegisterRequest;
import com.medvault.dto.request.ResetPasswordRequest;
import com.medvault.dto.response.ApiResponse;
import com.medvault.dto.response.LoginResponse;
import com.medvault.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register
     * Register a new user (any role).
     * Status will be PENDING until admin approves.
     */
    @PostMapping(value = "/register", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ApiResponse<String>> register(
            @RequestPart("data") @Valid RegisterRequest request,
            @RequestPart(value = "profilePhoto", required = false) MultipartFile profilePhoto,
            @RequestPart(value = "idCard",       required = false) MultipartFile idCard,
            @RequestPart(value = "degreeCert",   required = false) MultipartFile degreeCert,
            @RequestPart(value = "licenceCard",  required = false) MultipartFile licenceCard) {
        return ResponseEntity.ok(authService.register(request, profilePhoto, idCard, degreeCert, licenceCard));
    }

    /**
     * POST /api/auth/login
     * Login with email and password.
     * Returns JWT token if ACTIVE, or status message if PENDING/REJECTED.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * POST /api/auth/reset-password/{userId}
     * Called on first login to set a new password.
     */
    @PostMapping("/reset-password/{userId}")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @PathVariable Long userId,
            @Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(userId, request));
    }
}