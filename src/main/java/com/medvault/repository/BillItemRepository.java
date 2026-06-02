//package com.medvault.repository;
//
//import com.medvault.entity.BillItem;
//import org.springframework.data.jpa.repository.JpaRepository;
//import java.util.List;
//
//public interface BillItemRepository extends JpaRepository<BillItem, Long> {
//    List<BillItem> findByBillId(Long billId);
//    void deleteByBillId(Long billId);
//}

package com.medvault.repository;

import com.medvault.entity.BillItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BillItemRepository extends JpaRepository<BillItem, Long> {

    // ── Used in BillingServiceImpl.toResponse() ───────────────────────────
    List<BillItem> findByBillId(Long billId);

    // ── Bulk delete when cancelling a bill ────────────────────────────────
    void deleteByBillId(Long billId);
}