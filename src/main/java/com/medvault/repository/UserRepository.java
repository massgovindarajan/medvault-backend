package com.medvault.repository;

import com.medvault.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    // ✅ FIX: was missing — used by DataSeeder to check if user exists
    boolean existsByEmail(String email);

    // L1 pending
    List<User> findByL1ApprovedFalseAndStatus(String status);

    // L2 pending
    List<User> findByL1ApprovedTrueAndL2ApprovedFalseAndStatus(String status);

    // By status
    List<User> findByStatus(String status);

    // By role
    List<User> findByRole(String role);
}