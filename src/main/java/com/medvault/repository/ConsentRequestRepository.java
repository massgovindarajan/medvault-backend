package com.medvault.repository;

import com.medvault.entity.ConsentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsentRequestRepository extends JpaRepository<ConsentRequest, Long> {
    List<ConsentRequest> findByPatientIdAndStatusOrderByRequestedAtDesc(Long patientId, String status);
    List<ConsentRequest> findByPatientIdOrderByRequestedAtDesc(Long patientId);
    List<ConsentRequest> findByDoctorIdOrderByRequestedAtDesc(Long doctorId);
    Optional<ConsentRequest> findByDoctorIdAndRecordIdAndStatus(Long doctorId, Long recordId, String status);
    boolean existsByDoctorIdAndRecordIdAndStatus(Long doctorId, Long recordId, String status);

    @Transactional
    @Modifying
    @Query("DELETE FROM ConsentRequest c WHERE c.patient.id = :userId")
    void deleteByPatientId(@Param("userId") Long userId);
    @Transactional
    @Modifying
    @Query("DELETE FROM ConsentRequest c WHERE c.doctor.id = :userId")
    void deleteByDoctorId(@Param("userId") Long userId);
}