package com.medvault.repository;

import com.medvault.entity.DoctorSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DoctorSlotRepository extends JpaRepository<DoctorSlot, Long> {

    List<DoctorSlot> findByDoctorId(Long doctorId);

    // used by AdminServiceImpl.deleteUser()
    void deleteByDoctorId(Long doctorId);

    // used by AppointmentServiceImpl — getSlotsByDoctorAndDate()
    List<DoctorSlot> findByDoctorIdAndDate(Long doctorId, LocalDate date);

    // used by AppointmentServiceImpl — todaySlots (active only)
    List<DoctorSlot> findByDoctorIdAndDateAndIsActiveTrue(Long doctorId, LocalDate date);
}