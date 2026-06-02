// com/medvault/service/impl/NotificationServiceImpl.java
package com.medvault.service.impl;

import com.medvault.dto.response.NotificationResponse;
import com.medvault.entity.Notification;
import com.medvault.entity.User;
import com.medvault.repository.NotificationRepository;
import com.medvault.repository.UserRepository;
import com.medvault.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notifRepo;
    private final UserRepository         userRepo;

    // userId → SseEmitter (one active connection per user)
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    // ══════════════════════════════════════════════════════════
    // SEND
    // ══════════════════════════════════════════════════════════

    @Override
    @Async
    @Transactional
    public void send(Long userId, String title, String message,
                     String type, String severity, String actionUrl, Long refId) {

        User user = userRepo.findById(userId).orElse(null);
        if (user == null) {
            log.warn("Notification skipped — user {} not found", userId);
            return;
        }

        Notification notif = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .severity(severity)
                .actionUrl(actionUrl)
                .refId(refId)
                .read(false)
                .build();

        notif = notifRepo.save(notif);
        pushToEmitter(userId, toResponse(notif));
    }

    // ── Convenience overloads ─────────────────────────────────────────────

    @Override
    public void sendInfo(Long userId, String title, String message,
                         String actionUrl, Long refId) {
        send(userId, title, message, "SYSTEM", "INFO", actionUrl, refId);
    }

    @Override
    public void sendSuccess(Long userId, String title, String message,
                            String actionUrl, Long refId) {
        send(userId, title, message, "SYSTEM", "SUCCESS", actionUrl, refId);
    }

    @Override
    public void sendWarning(Long userId, String title, String message,
                            String actionUrl, Long refId) {
        send(userId, title, message, "SYSTEM", "WARNING", actionUrl, refId);
    }

    @Override
    public void sendError(Long userId, String title, String message,
                          String actionUrl, Long refId) {
        send(userId, title, message, "SYSTEM", "ERROR", actionUrl, refId);
    }

    // ── Broadcast to all users with a given role ──────────────────────────

    @Override
    @Async
    @Transactional
    public void broadcast(String role, String title, String message,
                          String type, String severity, String actionUrl) {

        List<User> targets = userRepo.findByRole(role);
        for (User user : targets) {
            Notification notif = Notification.builder()
                    .user(user)
                    .title(title)
                    .message(message)
                    .type(type)
                    .severity(severity)
                    .actionUrl(actionUrl)
                    .read(false)
                    .build();
            notif = notifRepo.save(notif);
            pushToEmitter(user.getId(), toResponse(notif));
        }

        log.info("Broadcast to {} users with role={}", targets.size(), role);
    }

    // ══════════════════════════════════════════════════════════
    // QUERY
    // ══════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getHistory(Long userId, int limit) {
        return notifRepo
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notifRepo.countByUserIdAndReadFalse(userId);
    }

    // ══════════════════════════════════════════════════════════
    // ACTIONS
    // ══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public void markRead(Long notificationId) {
        notifRepo.markAsRead(notificationId);
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        notifRepo.markAllReadForUser(userId);
    }

    // ══════════════════════════════════════════════════════════
    // SSE EMITTER MANAGEMENT
    // ══════════════════════════════════════════════════════════

    @Override
    public SseEmitter registerEmitter(Long userId) {
        // Remove any existing emitter for this user
        removeEmitter(userId);

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitter.onCompletion(() -> removeEmitter(userId));
        emitter.onTimeout(()    -> removeEmitter(userId));
        emitter.onError(e      -> removeEmitter(userId));

        emitters.put(userId, emitter);

        // Send a "connected" heartbeat immediately
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("{\"status\":\"connected\",\"userId\":" + userId + "}"));
        } catch (IOException e) {
            removeEmitter(userId);
        }

        log.info("SSE emitter registered for userId={}", userId);
        return emitter;
    }

    @Override
    public void removeEmitter(Long userId) {
        SseEmitter old = emitters.remove(userId);
        if (old != null) {
            try { old.complete(); } catch (Exception ignored) {}
        }
    }

    // ── Push one notification to the user's live SSE connection ──────────

    private void pushToEmitter(Long userId, NotificationResponse payload) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) return;

        try {
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(payload));
        } catch (IOException e) {
            log.warn("SSE push failed for userId={} — removing emitter", userId);
            removeEmitter(userId);
        }
    }

    // ══════════════════════════════════════════════════════════
    // MAPPER
    // ══════════════════════════════════════════════════════════

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .severity(n.getSeverity())
                .actionUrl(n.getActionUrl())
                .refId(n.getRefId())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}