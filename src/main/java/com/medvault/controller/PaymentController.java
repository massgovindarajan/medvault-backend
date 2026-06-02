//package com.medvault.controller;
//
//import com.medvault.service.BillingService;
//import com.razorpay.Order;
//import com.razorpay.RazorpayClient;
//import com.razorpay.Utils;
//import lombok.RequiredArgsConstructor;
//import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/payments")
//@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:4200")
//public class PaymentController {
//
//    private final BillingService billingService;
//
//    @Value("${razorpay.key.id}")
//    private String keyId;
//
//    @Value("${razorpay.key.secret}")
//    private String keySecret;
//
//    @Value("${razorpay.webhook.secret}")
//    private String webhookSecret;
//
//    // CREATE ORDER
//    @PostMapping("/create-order")
//    public ResponseEntity<Map<String, Object>> createOrder(
//            @RequestBody Map<String, Object> req) {
//
//        try {
//
//            RazorpayClient client =
//                    new RazorpayClient(keyId, keySecret);
//
//            int amountPaise = (int)
//                    (Double.parseDouble(
//                            req.getOrDefault("amount", 500).toString()
//                    ) * 100);
//
//            JSONObject orderRequest = new JSONObject();
//
//            orderRequest.put("amount", amountPaise);
//
//            orderRequest.put("currency", "INR");
//
//            orderRequest.put(
//                    "receipt",
//                    req.getOrDefault(
//                            "billRef",
//                            "BILL-" + LocalDateTime.now()
//                    )
//            );
//
//            orderRequest.put("payment_capture", 1);
//
//            Order order = client.orders.create(orderRequest);
//
//            Map<String, Object> response =
//                    new HashMap<>();
//
//            response.put("success", true);
//
//            response.put("orderId",
//                    order.get("id").toString());
//
//            response.put("amount", amountPaise);
//
//            response.put("currency", "INR");
//
//            response.put("keyId", keyId);
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//
//            return ResponseEntity
//                    .status(500)
//                    .body(Map.of(
//                            "success", false,
//                            "message", e.getMessage()
//                    ));
//        }
//    }
//}
package com.medvault.controller;

import com.medvault.service.BillingService;
import com.razorpay.Order;
import com.razorpay.Refund;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:4200")
public class PaymentController {

    private final BillingService billingService;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    // ══════════════════════════════════════════════════════════
    // 1. CREATE ORDER
    // POST /api/payments/create-order
    // Body: { amount: 250, billRef: "APT-123456" }
    // ══════════════════════════════════════════════════════════
    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(
            @RequestBody Map<String, Object> req) {
        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            int amountPaise = (int) (
                Double.parseDouble(req.getOrDefault("amount", 250).toString()) * 100
            );

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount",          amountPaise);
            orderRequest.put("currency",        "INR");
            orderRequest.put("receipt",         req.getOrDefault("billRef", "BILL-" + LocalDateTime.now()).toString());
            orderRequest.put("payment_capture", 1);

            // Optional notes for Razorpay dashboard
            JSONObject notes = new JSONObject();
            notes.put("description", req.getOrDefault("description", "MedVault Appointment"));
            notes.put("hospital",    "MedVault");
            orderRequest.put("notes", notes);

            Order order = client.orders.create(orderRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("success",  true);
            response.put("orderId",  order.get("id").toString());
            response.put("amount",   amountPaise);
            response.put("currency", "INR");
            response.put("keyId",    keyId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    // ══════════════════════════════════════════════════════════
    // 2. VERIFY PAYMENT SIGNATURE
    // POST /api/payments/verify
    // Body: { orderId, paymentId, signature, billRef, amount }
    // ══════════════════════════════════════════════════════════
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(
            @RequestBody Map<String, String> req) {
        try {
            String orderId   = req.get("orderId");
            String paymentId = req.get("paymentId");
            String signature = req.get("signature");

            // Razorpay signature = HMAC-SHA256(orderId + "|" + paymentId, keySecret)
            String data      = orderId + "|" + paymentId;
            String generated = hmacSHA256(data, keySecret);
            boolean valid    = generated.equals(signature);

            if (valid) {
                // ── Optional: update billing/appointment status in DB ──
                // billingService.markPaymentSuccess(req.get("billRef"), paymentId, orderId);
                return ResponseEntity.ok(Map.of(
                    "success",   true,
                    "paymentId", paymentId,
                    "orderId",   orderId,
                    "message",   "Payment verified successfully"
                ));
            } else {
                return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", "Signature mismatch – payment not verified"
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    // ══════════════════════════════════════════════════════════
    // 3. REFUND
    // POST /api/payments/refund/{paymentId}
    // Body: { amount: 250, reason: "Doctor unavailable" }
    // ══════════════════════════════════════════════════════════
    @PostMapping("/refund/{paymentId}")
    public ResponseEntity<Map<String, Object>> refund(
            @PathVariable String paymentId,
            @RequestBody Map<String, Object> req) {
        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            Object amountRaw = req.getOrDefault("amount", "250");
              int amountPaise  = (int) (Double.parseDouble(amountRaw.toString()) * 100);

            JSONObject params = new JSONObject();
            params.put("amount", amountPaise);

            JSONObject notes = new JSONObject();
            notes.put("reason", req.getOrDefault("reason", "Refund requested"));
            params.put("notes", notes);

            Refund refund = client.payments.refund(paymentId, params);

            // ── Optional: update billing record ──
            // billingService.markRefunded(paymentId, refund.get("id").toString());

            return ResponseEntity.ok(Map.of(
                "success",  true,
                "refundId", refund.get("id").toString(),
                "amount",   amountPaise / 100,
                "message",  "Refund initiated successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    // ══════════════════════════════════════════════════════════
    // 4. WEBHOOK — Razorpay server-to-server events
    // POST /api/payments/webhook
    // Header: X-Razorpay-Signature
    // Configure URL in Razorpay Dashboard → Settings → Webhooks
    // Events: payment.captured, payment.failed, order.paid
    // ══════════════════════════════════════════════════════════
    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        try {
            // Verify webhook authenticity
            if (signature == null || signature.isBlank()) {
                return ResponseEntity.status(400).body("Missing signature");
            }

            boolean valid = Utils.verifyWebhookSignature(payload, signature, webhookSecret);
            if (!valid) {
                return ResponseEntity.status(400).body("Invalid webhook signature");
            }

            JSONObject event     = new JSONObject(payload);
            String     eventType = event.getString("event");

            switch (eventType) {

                case "payment.captured" -> {
                    JSONObject payment   = event.getJSONObject("payload")
                                               .getJSONObject("payment")
                                               .getJSONObject("entity");
                    String paymentId     = payment.getString("id");
                    String orderId       = payment.getString("order_id");
                    int    amount        = payment.getInt("amount") / 100; // back to rupees

                    // ── Mark appointment as payment confirmed in DB ──
                    // billingService.markPaymentCaptured(paymentId, orderId, amount);
                    System.out.println("✅ Payment captured: " + paymentId + " ₹" + amount);
                }

                case "payment.failed" -> {
                    JSONObject payment = event.getJSONObject("payload")
                                             .getJSONObject("payment")
                                             .getJSONObject("entity");
                    String paymentId   = payment.getString("id");
                    String errorDesc   = payment.optString("error_description", "Unknown error");

                    // ── Handle failed payment ──
                    // billingService.markPaymentFailed(paymentId, errorDesc);
                    System.out.println("❌ Payment failed: " + paymentId + " — " + errorDesc);
                }

                case "order.paid" -> {
                    JSONObject order = event.getJSONObject("payload")
                                           .getJSONObject("order")
                                           .getJSONObject("entity");
                    String orderId   = order.getString("id");
                    String receipt   = order.optString("receipt", "");

                    // ── Mark appointment booking fully paid ──
                    // billingService.markOrderPaid(orderId, receipt);
                    System.out.println("✅ Order paid: " + orderId + " receipt: " + receipt);
                }

                default -> System.out.println("ℹ️ Unhandled webhook event: " + eventType);
            }

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            System.err.println("Webhook error: " + e.getMessage());
            return ResponseEntity.status(500).body("Webhook processing failed");
        }
    }

    // ══════════════════════════════════════════════════════════
    // HELPER — HMAC-SHA256 for signature verification
    // ══════════════════════════════════════════════════════════
    private String hmacSHA256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
        byte[] hash = mac.doFinal(data.getBytes("UTF-8"));
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) hex.append(String.format("%02x", b));
        return hex.toString();
    }
}