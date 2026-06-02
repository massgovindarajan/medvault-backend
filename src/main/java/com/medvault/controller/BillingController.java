//package com.medvault.controller;
//
//import com.medvault.dto.request.BillRequest;
//import com.medvault.dto.request.PaymentRequest;
//import com.medvault.dto.response.ApiResponse;
//import com.medvault.dto.response.BillResponse;
//import com.medvault.repository.UserRepository;
//import com.medvault.service.BillingService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//
//@RestController
//@RequestMapping("/api/billing")
//@RequiredArgsConstructor
//public class BillingController {
//
//    private final BillingService billingService;
//    private final UserRepository userRepository;
//
//    /** Receptionist generates a new bill */
//    @PostMapping("/bills")
//    public ResponseEntity<ApiResponse<BillResponse>> generate(
//            @RequestBody BillRequest req,
//            @AuthenticationPrincipal UserDetails principal) {
//        return ResponseEntity.ok(billingService.generateBill(getUserId(principal), req));
//    }
//
//    /** All bills — receptionist / admin */
//    @GetMapping("/bills")
//    public ResponseEntity<List<BillResponse>> all() {
//        return ResponseEntity.ok(billingService.getAllBills());
//    }
//
//    /** Pending bills only */
//    @GetMapping("/bills/pending")
//    public ResponseEntity<List<BillResponse>> pending() {
//        return ResponseEntity.ok(billingService.getBillsByStatus("PENDING"));
//    }
//
//    /** Patient views their own bills */
//    @GetMapping("/bills/patient")
//    public ResponseEntity<List<BillResponse>> myBills(
//            @AuthenticationPrincipal UserDetails principal) {
//        return ResponseEntity.ok(billingService.getPatientBills(getUserId(principal)));
//    }
//
//    /** Single bill by id */
//    @GetMapping("/bills/{id}")
//    public ResponseEntity<BillResponse> one(@PathVariable Long id) {
//        return ResponseEntity.ok(billingService.getBillById(id));
//    }
//
//    /** Collect payment — marks bill as PAID, records method + ref */
//    @PutMapping("/bills/{id}/pay")
//    public ResponseEntity<ApiResponse<BillResponse>> pay(
//            @PathVariable Long id,
//            @RequestBody PaymentRequest req,
//            @AuthenticationPrincipal UserDetails principal) {
//        return ResponseEntity.ok(billingService.collectPayment(id, getUserId(principal), req));
//    }
//
//    /** Cancel bill */
//    @PutMapping("/bills/{id}/cancel")
//    public ResponseEntity<ApiResponse<BillResponse>> cancel(
//            @PathVariable Long id,
//            @AuthenticationPrincipal UserDetails principal) {
//        return ResponseEntity.ok(billingService.cancelBill(id, getUserId(principal)));
//    }
//
//    /** Dashboard summary stats */
//    @GetMapping("/summary")
//    public ResponseEntity<BillingService.BillingSummary> summary() {
//        return ResponseEntity.ok(billingService.getSummary());
//    }
//
//    private Long getUserId(UserDetails principal) {
//        return userRepository.findByEmail(principal.getUsername())
//                .orElseThrow(() -> new RuntimeException("User not found"))
//                .getId();
//    }
//}




package com.medvault.controller;

import com.medvault.dto.request.BillRequest;
import com.medvault.dto.request.PaymentRequest;
import com.medvault.dto.response.ApiResponse;
import com.medvault.dto.response.BillResponse;
import com.medvault.repository.UserRepository;
import com.medvault.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;
    private final UserRepository userRepository;

    /** Receptionist generates a new bill */
    @PostMapping("/bills")
    public ResponseEntity<ApiResponse<BillResponse>> generate(
            @RequestBody BillRequest req,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(billingService.generateBill(getUserId(principal), req));
    }

    /** All bills — receptionist / admin */
    @GetMapping("/bills")
    public ResponseEntity<List<BillResponse>> all() {
        return ResponseEntity.ok(billingService.getAllBills());
    }

    /** Pending bills only */
    @GetMapping("/bills/pending")
    public ResponseEntity<List<BillResponse>> pending() {
        return ResponseEntity.ok(billingService.getBillsByStatus("PENDING"));
    }

    /** Patient views their own bills */
    @GetMapping("/bills/patient")
    public ResponseEntity<List<BillResponse>> myBills(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(billingService.getPatientBills(getUserId(principal)));
    }

    /** Single bill by id */
    @GetMapping("/bills/{id}")
    public ResponseEntity<BillResponse> one(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getBillById(id));
    }

    /** Collect payment — marks bill as PAID, records method + ref */
    @PutMapping("/bills/{id}/pay")
    public ResponseEntity<ApiResponse<BillResponse>> pay(
            @PathVariable Long id,
            @RequestBody PaymentRequest req,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(billingService.collectPayment(id, getUserId(principal), req));
    }

    /** Cancel bill */
    @PutMapping("/bills/{id}/cancel")
    public ResponseEntity<ApiResponse<BillResponse>> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(billingService.cancelBill(id, getUserId(principal)));
    }

    /** Dashboard summary stats */
    @GetMapping("/summary")
    public ResponseEntity<BillingService.BillingSummary> summary() {
        return ResponseEntity.ok(billingService.getSummary());
    }

    private Long getUserId(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}