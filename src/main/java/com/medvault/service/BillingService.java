package com.medvault.service;

import com.medvault.dto.request.BillRequest;
import com.medvault.dto.request.PaymentRequest;
import com.medvault.dto.response.ApiResponse;
import com.medvault.dto.response.BillResponse;

import java.util.List;

public interface BillingService {

    ApiResponse<BillResponse> generateBill(
            Long staffId,
            BillRequest req
    );

    List<BillResponse> getAllBills();

    List<BillResponse> getBillsByStatus(String status);

    List<BillResponse> getPatientBills(Long patientId);

    BillResponse getBillById(Long id);

    ApiResponse<BillResponse> collectPayment(
            Long billId,
            Long staffId,
            PaymentRequest req
    );

    ApiResponse<BillResponse> cancelBill(
            Long billId,
            Long staffId
    );

    // ═══════════════════════════════════════
    // RAZORPAY METHODS
    // ═══════════════════════════════════════

    void processWebhookPayment(String receipt, String paymentId, String orderId, int amount);
    void markPaymentFailed(String orderId, String errorDesc);
    void recordRefund(String paymentId, int amount, String refundId);

    // ═══════════════════════════════════════
    // SUMMARY
    // ═══════════════════════════════════════

    BillingSummary getSummary();

    record BillingSummary(
            long totalBills,
            long pendingBills,
            long paidBills,
            double totalRevenue,
            double pendingAmount
    ) {}
}