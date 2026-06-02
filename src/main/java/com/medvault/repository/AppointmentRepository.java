package com.medvault.repository;

import com.medvault.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    List<Appointment> findByDoctorIdOrderByAppointmentDateAscAppointmentTimeAsc(Long doctorId);

    List<Appointment> findByDoctorIdAndStatusOrderByCreatedAtDesc(Long doctorId, String status);

    List<Appointment> findAllByOrderByCreatedAtDesc();

    // ✅ Needed for AdminServiceImpl.deleteUser()
    void deleteByPatientId(Long patientId);

    void deleteByDoctorId(Long doctorId);
}