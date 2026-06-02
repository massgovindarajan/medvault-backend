package com.medvault.repository;

import com.medvault.entity.MedicalCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedicalCertificateRepository extends JpaRepository<MedicalCertificate, Long> {
    List<MedicalCertificate> findByUserId(Long userId);
    void deleteByUserId(Long userId);  // ✅ ADDED
}