//package com.medvault.repository;
//
//import com.medvault.entity.Bill;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface BillRepository extends JpaRepository<Bill, Long> {
//
//    List<Bill> findByPatientIdOrderByCreatedAtDesc(Long patientId);
//
//    List<Bill> findByStatusOrderByCreatedAtDesc(String status);
//
//    List<Bill> findAllByOrderByCreatedAtDesc();
//
//    Optional<Bill> findByBillNumber(String billNumber);
//    Optional<Bill> findByBillRef(String billRef);
//
//    boolean existsByAppointmentId(Long appointmentId);
//
//    Optional<Bill> findByAppointmentId(Long appointmentId);
//
//    @Query("SELECT COUNT(b) FROM Bill b WHERE b.status = 'PAID'")
//    Long countPaid();
//
//    @Query("SELECT COALESCE(SUM(b.paidAmount), 0) FROM Bill b WHERE b.status = 'PAID'")
//    Double totalRevenue();
//
//    void deleteByPatientId(Long patientId);
//}

package com.medvault.repository;

import com.medvault.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {

    // ── Used by BillingServiceImpl ────────────────────────────────────────
    List<Bill> findAllByOrderByCreatedAtDesc();

    List<Bill> findByStatusOrderByCreatedAtDesc(String status);

    List<Bill> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    // ── Used by processWebhookPayment ─────────────────────────────────────
    Optional<Bill> findByBillNumber(String billNumber);

    // ── Used by PaymentController / reporting ─────────────────────────────
    List<Bill> findByPatientId(Long patientId);

    @Query("SELECT b FROM Bill b WHERE b.createdAt BETWEEN :from AND :to ORDER BY b.createdAt DESC")
    List<Bill> findByDateRange(LocalDateTime from, LocalDateTime to);

    @Query("SELECT b FROM Bill b WHERE b.status = 'PENDING' AND b.dueAmount > 0 ORDER BY b.createdAt ASC")
    List<Bill> findAllPendingWithDues();
}