package com.medvault.repository;

import com.medvault.entity.IdentityDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IdentityDocumentRepository extends JpaRepository<IdentityDocument, Long> {
    List<IdentityDocument> findByUserId(Long userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM IdentityDocument i WHERE i.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}