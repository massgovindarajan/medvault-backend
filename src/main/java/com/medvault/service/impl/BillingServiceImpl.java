package com.medvault.service.impl;

import com.medvault.dto.request.BillItemRequest;
import com.medvault.dto.request.BillRequest;
import com.medvault.dto.request.PaymentRequest;
import com.medvault.dto.response.ApiResponse;
import com.medvault.dto.response.BillItemResponse;
import com.medvault.dto.response.BillResponse;
import com.medvault.entity.*;
import com.medvault.repository.*;
import com.medvault.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final BillRepository            billRepository;
    private final BillItemRepository        billItemRepository;
    private final UserRepository            userRepository;
    private final AppointmentRepository     appointmentRepository;
    private final PersonalDetailsRepository personalDetailsRepository;
    private final PrescriptionRepository    prescriptionRepository;
    private final MedicineInventoryRepository medicineInventoryRepository;
    private final PaymentRepository paymentRepository;

    private static final AtomicLong billSeq = new AtomicLong(1000);

    // ═══════════════════════════════════════════
    // GENERATE BILL
    // ═══════════════════════════════════════════

    @Override
    @Transactional
    public ApiResponse<BillResponse> generateBill(Long staffId, BillRequest req) {
        User patient = userRepository.findById(req.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        Appointment appt = null;
        if (req.getAppointmentId() != null) {
            appt = appointmentRepository.findById(req.getAppointmentId()).orElse(null);
        }

        Prescription prescription = null;
        if (req.getPrescriptionId() != null) {
            prescription = prescriptionRepository.findById(req.getPrescriptionId()).orElse(null);
        }

        // Build bill number: BILL-2026-01042
        String billNumber = "BILL-" + LocalDateTime.now().getYear()
                + "-" + String.format("%05d", billSeq.getAndIncrement());

        Bill bill = Bill.builder()
                .billNumber(billNumber)
                .patient(patient)
                .appointment(appt)
                .prescription(prescription)
                .status("PENDING")
                .discountAmount(req.getDiscountAmount() != null ? req.getDiscountAmount() : 0.0)
                .notes(req.getNotes())
                .generatedBy(staff)
                .build();

        Bill saved = billRepository.save(bill);

        // Build line items
        List<BillItem> items = new ArrayList<>();
        double subtotal = 0.0;

        // 1. Items from request
        if (req.getItems() != null) {
            for (BillItemRequest ir : req.getItems()) {
                int qty = ir.getQuantity() != null ? ir.getQuantity() : 1;
                double total = qty * ir.getUnitPrice();
                items.add(BillItem.builder()
                        .bill(saved)
                        .category(ir.getCategory())
                        .description(ir.getDescription())
                        .quantity(qty)
                        .unitPrice(ir.getUnitPrice())
                        .totalPrice(total)
                        .build());
                subtotal += total;
            }
        }

        // 2. Auto-add medicine items from prescription if linked
        if (prescription != null) {
            for (PrescriptionItem pi : prescription.getItems()) {
                if ("DISPENSED".equals(pi.getStatus())) {
                    Double price = medicineInventoryRepository
                            .findByMedicineNameIgnoreCase(pi.getMedicineName())
                            .map(MedicineInventory::getUnitPrice)
                            .orElse(0.0);
                    if (price == null) price = 0.0;
                    int qty = pi.getQuantity() != null ? pi.getQuantity() : 1;
                    double total = qty * price;
                    items.add(BillItem.builder()
                            .bill(saved)
                            .category("MEDICINE")
                            .description(pi.getMedicineName() +
                                    (pi.getDosage() != null ? " – " + pi.getDosage() : ""))
                            .quantity(qty)
                            .unitPrice(price)
                            .totalPrice(total)
                            .build());
                    subtotal += total;
                }
            }
        }

        billItemRepository.saveAll(items);
        saved.setItems(items);

        // Calculate totals
        double discount = req.getDiscountAmount() != null ? req.getDiscountAmount() : 0.0;
        double taxPct   = req.getTaxPercent()    != null ? req.getTaxPercent()    : 0.0;
        double taxAmt   = (subtotal - discount) * taxPct / 100.0;
        double total    = subtotal - discount + taxAmt;

        saved.setTotalAmount(Math.max(total, 0.0));
        saved.setTaxAmount(taxAmt);
        saved.setDiscountAmount(discount);
        saved.setDueAmount(Math.max(total, 0.0));
        saved.setPaidAmount(0.0);
        billRepository.save(saved);

        log.info("Bill #{} generated for patient={} by staff={} total={}",
                billNumber, patient.getId(), staffId, total);

        return ApiResponse.ok("Bill #" + billNumber + " generated successfully!", toResponse(saved));
    }

    // ═══════════════════════════════════════════
    // QUERIES
    // ═══════════════════════════════════════════

    @Override @Transactional(readOnly = true)
    public List<BillResponse> getAllBills() {
        return billRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override @Transactional(readOnly = true)
    public List<BillResponse> getBillsByStatus(String status) {
        return billRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override @Transactional(readOnly = true)
    public List<BillResponse> getPatientBills(Long patientId) {
        return billRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override @Transactional(readOnly = true)
    public BillResponse getBillById(Long id) {
        return toResponse(billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found: " + id)));
    }

    // ═══════════════════════════════════════════
    // COLLECT PAYMENT
    // ═══════════════════════════════════════════

    @Override
    @Transactional
    public ApiResponse<BillResponse> collectPayment(Long billId, Long staffId, PaymentRequest req) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found: " + billId));

        if ("PAID".equals(bill.getStatus())) {
            return ApiResponse.error("Bill #" + bill.getBillNumber() + " is already paid.");
        }
        if ("CANCELLED".equals(bill.getStatus())) {
            return ApiResponse.error("Cannot collect payment for a cancelled bill.");
        }

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        double paid    = req.getPaidAmount() != null ? req.getPaidAmount() : bill.getTotalAmount();
        double due     = bill.getTotalAmount() - paid;

        bill.setPaymentMethod(req.getPaymentMethod());
        bill.setPaidAmount(paid);
        bill.setDueAmount(Math.max(due, 0.0));
        bill.setTransactionRef(req.getTransactionRef());
        bill.setStatus("PAID");
        bill.setPaidAt(LocalDateTime.now());
        bill.setPaidBy(staff);
        if (req.getNotes() != null) bill.setNotes(req.getNotes());

        billRepository.save(bill);

        log.info("Bill #{} PAID — amount={} method={} by staff={}",
                bill.getBillNumber(), paid, req.getPaymentMethod(), staffId);

        return ApiResponse.ok("Payment collected! Bill #" + bill.getBillNumber() + " is now PAID.", toResponse(bill));
    }

    // ═══════════════════════════════════════════
    // CANCEL BILL
    // ═══════════════════════════════════════════

    @Override
    @Transactional
    public ApiResponse<BillResponse> cancelBill(Long billId, Long staffId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found: " + billId));
        if ("PAID".equals(bill.getStatus())) {
            return ApiResponse.error("Cannot cancel a paid bill.");
        }
        bill.setStatus("CANCELLED");
        billRepository.save(bill);
        log.info("Bill #{} cancelled by staff={}", bill.getBillNumber(), staffId);
        return ApiResponse.ok("Bill #" + bill.getBillNumber() + " cancelled.", toResponse(bill));
    }

    // ═══════════════════════════════════════════
    // SUMMARY
    // ═══════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public BillingSummary getSummary() {
        List<Bill> all     = billRepository.findAll();
        long pending       = all.stream().filter(b -> "PENDING".equals(b.getStatus())).count();
        long paid          = all.stream().filter(b -> "PAID".equals(b.getStatus())).count();
        double revenue     = all.stream().filter(b -> "PAID".equals(b.getStatus())).mapToDouble(Bill::getPaidAmount).sum();
        double pendingAmt  = all.stream().filter(b -> "PENDING".equals(b.getStatus())).mapToDouble(Bill::getTotalAmount).sum();
        return new BillingSummary(all.size(), pending, paid, revenue, pendingAmt);
    }
    
    
    // ═══════════════════════════════════════════
    // RAZORPAY WEBHOOK PAYMENT
    // ═══════════════════════════════════════════

    @Override
    @Transactional
    public void processWebhookPayment(
            String receipt,
            String paymentId,
            String orderId,
            int amount
    ) {

        Bill bill = billRepository.findByBillNumber(receipt)
                .orElse(null);

        if (bill == null) {

            log.error("Bill not found for receipt={}", receipt);

            return;
        }

        bill.setStatus("PAID");

        bill.setPaymentMethod("ONLINE");

        bill.setTransactionRef(paymentId);

        bill.setPaidAmount((double) amount / 100.0);

        bill.setDueAmount(0.0);

        bill.setPaidAt(LocalDateTime.now());

        billRepository.save(bill);

        Payment payment = new Payment();

        payment.setBill(bill);

        payment.setMethod("RAZORPAY");

        payment.setAmount((long) (amount / 100));

        payment.setStatus("CAPTURED");

        payment.setRazorpayPaymentId(paymentId);

        payment.setRazorpayOrderId(orderId);

        payment.setCreatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        log.info("Webhook payment success for bill={}", receipt);
    }

    // ═══════════════════════════════════════════
    // PAYMENT FAILED
    // ═══════════════════════════════════════════

    @Override
    @Transactional
    public void markPaymentFailed(
            String orderId,
            String errorDesc
    ) {

        paymentRepository.findByRazorpayOrderId(orderId)
                .ifPresent(payment -> {

                    payment.setStatus("FAILED");

                    payment.setNotes(errorDesc);

                    paymentRepository.save(payment);

                    log.error(
                            "Payment failed orderId={} reason={}",
                            orderId,
                            errorDesc
                    );
                });
    }

    // ═══════════════════════════════════════════
    // REFUND
    // ═══════════════════════════════════════════

    @Override
    @Transactional
    public void recordRefund(
            String paymentId,
            int amount,
            String refundId
    ) {

        paymentRepository.findByRazorpayPaymentId(paymentId)
                .ifPresent(payment -> {

                    payment.setRefundId(refundId);

                    payment.setRefundedAmount(amount);

                    payment.setStatus("REFUNDED");

                    paymentRepository.save(payment);

                    Bill bill = payment.getBill();

                    if (bill != null) {

                        bill.setStatus("REFUNDED");

                        billRepository.save(bill);
                    }

                    log.info(
                            "Refund recorded paymentId={} refundId={}",
                            paymentId,
                            refundId
                    );
                });
    }

    // ═══════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════

    private BillResponse toResponse(Bill b) {
        String patientName  = getName(b.getPatient().getId());
        String patientPhone = personalDetailsRepository.findByUserId(b.getPatient().getId())
                .map(pd -> pd.getPhone() != null ? pd.getPhone() : "").orElse("");
        String doctorName   = b.getAppointment() != null
                ? getName(b.getAppointment().getDoctor().getId()) : null;
        String genByName    = b.getGeneratedBy() != null ? getName(b.getGeneratedBy().getId()) : null;
        String paidByName   = b.getPaidBy() != null ? getName(b.getPaidBy().getId()) : null;

        List<BillItemResponse> items = billItemRepository.findByBillId(b.getId()).stream()
                .map(i -> BillItemResponse.builder()
                        .id(i.getId())
                        .category(i.getCategory())
                        .description(i.getDescription())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .totalPrice(i.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        return BillResponse.builder()
                .id(b.getId())
                .billNumber(b.getBillNumber())
                .patientId(b.getPatient().getId())
                .patientName(patientName)
                .patientEmail(b.getPatient().getEmail())
                .patientPhone(patientPhone)
                .appointmentId(b.getAppointment() != null ? b.getAppointment().getId() : null)
                .appointmentDate(b.getAppointment() != null ? b.getAppointment().getAppointmentDate() : null)
                .doctorName(doctorName)
                .prescriptionId(b.getPrescription() != null ? b.getPrescription().getId() : null)
                .status(b.getStatus())
                .paymentMethod(b.getPaymentMethod())
                .totalAmount(b.getTotalAmount())
                .discountAmount(b.getDiscountAmount())
                .taxAmount(b.getTaxAmount())
                .paidAmount(b.getPaidAmount())
                .dueAmount(b.getDueAmount())
                .transactionRef(b.getTransactionRef())
                .notes(b.getNotes())
                .generatedByName(genByName)
                .paidByName(paidByName)
                .paidAt(b.getPaidAt() != null ? b.getPaidAt().toString() : null)
                .createdAt(b.getCreatedAt() != null ? b.getCreatedAt().toString() : "")
                .items(items)
                .build();
    }
    

    private String getName(Long userId) {
        return personalDetailsRepository.findByUserId(userId)
                .map(pd -> (pd.getFirstName() + " " + pd.getLastName()).trim())
                .orElse("Unknown");
    }

	
}