package com.medvault.repository;

import com.medvault.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    List<MedicalRecord> findByPatientId(Long patientId);

    // used in MedicalRecordServiceImpl.getMyRecords()
    List<MedicalRecord> findByPatientIdAndIsActiveTrueOrderByCreatedAtDesc(Long patientId);

    // used by category filter
    List<MedicalRecord> findByPatientIdAndCategoryAndIsActiveTrueOrderByCreatedAtDesc(
            Long patientId, String category);

    // used by AdminServiceImpl.deleteUser()
    void deleteByPatientId(Long patientId);
}