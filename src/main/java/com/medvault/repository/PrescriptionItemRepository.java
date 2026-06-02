package com.medvault.repository;

import com.medvault.entity.PrescriptionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, Long> {
    List<PrescriptionItem> findByPrescriptionId(Long prescriptionId);
    void deleteByPrescriptionId(Long prescriptionId);
}