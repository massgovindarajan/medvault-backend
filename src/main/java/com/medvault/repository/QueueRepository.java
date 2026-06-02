package com.medvault.repository;

import com.medvault.entity.Queue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QueueRepository extends JpaRepository<Queue, Long> {

    /**
     * Finds the highest token number ever issued, sorted numerically on the
     * suffix so "T-9" never beats "T-100".
     * Returns null when the table is empty.
     */
    @Query(
        value  = "SELECT token_number FROM queue " +
                 "ORDER BY CAST(SUBSTRING(token_number, 3) AS UNSIGNED) DESC " +
                 "LIMIT 1",
        nativeQuery = true
    )
    String findMaxTokenNumber();
}