// com/medvault/service/NotificationService.java
package com.medvault.service;

import com.medvault.dto.response.NotificationResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.List;

public interface NotificationService {

    // ── Send ─────────────────────────────────────────────────────────────
    void send(Long userId, String title, String message,
              String type, String severity, String actionUrl, Long refId);

    // Convenience overloads
    void sendInfo   (Long userId, String title, String message, String actionUrl, Long refId);
    void sendSuccess(Long userId, String title, String message, String actionUrl, Long refId);
    void sendWarning(Long userId, String title, String message, String actionUrl, Long refId);
    void sendError  (Long userId, String title, String message, String actionUrl, Long refId);

    // Broadcast to all users of a role
    void broadcast(String role, String title, String message,
                   String type, String severity, String actionUrl);

    // ── Query ─────────────────────────────────────────────────────────────
    List<NotificationResponse> getHistory(Long userId, int limit);
    long                       getUnreadCount(Long userId);

    // ── Actions ───────────────────────────────────────────────────────────
    void markRead   (Long notificationId);
    void markAllRead(Long userId);

    // ── SSE ───────────────────────────────────────────────────────────────
    SseEmitter registerEmitter(Long userId);
    void       removeEmitter (Long userId);
}