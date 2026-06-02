package com.medvault.repository;

import com.medvault.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByUserId(Long userId);

    // ✅ Image 2 — used by ChatServiceImpl.getHistory()
    List<ChatMessage> findByUserIdOrderBySentAtDesc(Long userId);

    // used by AdminServiceImpl.deleteUser()
    void deleteByUserId(Long userId);
}