//package com.medvault.dto.request;
//
//import lombok.Data;
//
//@Data
//public class PaymentRequest {
//    
//    private String paymentMethod;
//    private Double paidAmount;
//    private String transactionRef;  
//    private String notes;
//}
package com.medvault.dto.request;

import lombok.Data;

@Data
public class PaymentRequest {

    // Amount the patient is paying now (defaults to full totalAmount if null)
    private Double paidAmount;

    // CASH | CARD | UPI | ONLINE
    private String paymentMethod;

    // Razorpay payment ID, UTR, cheque number, etc.
    private String transactionRef;

    private String notes;
}