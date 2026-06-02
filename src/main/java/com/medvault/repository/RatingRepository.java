package com.medvault.repository;

import com.medvault.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByDoctorIdOrderByRatedAtDesc(Long doctorId);

    Optional<Rating> findByAppointmentId(Long appointmentId);

    boolean existsByAppointmentId(Long appointmentId);

    @Query("SELECT COALESCE(AVG(r.stars), 0) FROM Rating r WHERE r.doctor.id = :doctorId")
    Double getAverageRatingByDoctorId(Long doctorId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Rating r WHERE r.doctor.id = :userId")
    void deleteByDoctorId(@Param("userId") Long userId);
    @Transactional
    @Modifying
    @Query("DELETE FROM Rating r WHERE r.patient.id = :userId")
    void deleteByPatientId(@Param("userId") Long userId);
}