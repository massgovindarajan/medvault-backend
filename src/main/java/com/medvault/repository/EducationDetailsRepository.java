package com.medvault.repository;

import com.medvault.entity.EducationDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EducationDetailsRepository extends JpaRepository<EducationDetails, Long> {
    List<EducationDetails> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}