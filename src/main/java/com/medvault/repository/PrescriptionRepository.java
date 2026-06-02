// ── PrescriptionRepository.java ───────────────────────────────
package com.medvault.repository;

import com.medvault.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

   
    List<Prescription> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);


    List<Prescription> findByPatientIdOrderByCreatedAtDesc(Long patientId);

   
    List<Prescription> findByStatusOrderByCreatedAtDesc(String status);

  
    List<Prescription> findAllByOrderByCreatedAtDesc();

  
    Optional<Prescription> findByAppointmentId(Long appointmentId);

    boolean existsByAppointmentId(Long appointmentId);
}