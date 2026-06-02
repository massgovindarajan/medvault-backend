package com.medvault.controller;

import com.medvault.dto.request.ApprovalRequest;
import com.medvault.dto.response.ApiResponse;
import com.medvault.dto.response.UserSummaryResponse;
import com.medvault.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
// ✅ No @PreAuthorize here — SecurityConfig already does:
//    .requestMatchers("/api/admin/**").authenticated()
//    Double-checking with @PreAuthorize was causing the 403.
public class AdminController {

    private final AdminService adminService;

    // ── Users ──────────────────────────────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<List<UserSummaryResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserSummaryResponse> getUserDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserDetail(id));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.deleteUser(id));
    }

    // ── L1 ─────────────────────────────────────────────────────────────
    @GetMapping("/l1/pending")
    public ResponseEntity<List<UserSummaryResponse>> getL1Pending() {
        return ResponseEntity.ok(adminService.getL1PendingUsers());
    }

    @PostMapping("/l1/approve")
    public ResponseEntity<ApiResponse<String>> l1Approve(
            @AuthenticationPrincipal UserDetails adminDetails,
            @RequestBody ApprovalRequest request) {
        return ResponseEntity.ok(adminService.l1Approve(1L, request));
    }

    // ── L2 ─────────────────────────────────────────────────────────────
    @GetMapping("/l2/pending")
    public ResponseEntity<List<UserSummaryResponse>> getL2Pending() {
        return ResponseEntity.ok(adminService.getL2PendingUsers());
    }

    @PostMapping("/l2/approve")
    public ResponseEntity<ApiResponse<String>> l2Approve(
            @AuthenticationPrincipal UserDetails adminDetails,
            @RequestBody ApprovalRequest request) {
        return ResponseEntity.ok(adminService.l2Approve(1L, request));
    }
}