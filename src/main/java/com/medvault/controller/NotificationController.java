// com/medvault/controller/NotificationController.java
package com.medvault.controller;
import java.util.Map;

import com.medvault.dto.response.NotificationResponse;
import com.medvault.entity.User;
import com.medvault.repository.UserRepository;
import com.medvault.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notifService;
    private final UserRepository      userRepo;

    // ── SSE stream — GET /api/notifications/stream ────────────────────────
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return notifService.registerEmitter(userId);
    }

    // ── History — GET /api/notifications?limit=20 ─────────────────────────
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getHistory(
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(notifService.getHistory(userId, limit));
    }

    // ── Unread count — GET /api/notifications/unread-count ───────────────
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(Map.of("count", notifService.getUnreadCount(userId)));
    
    }
    
    @PostMapping("/send")
    public ResponseEntity<Void> send(@RequestBody Map<String, Object> req) {
        // This is a receptionist manual send — log it and return OK
        // Wire to notifService.sendNotification() if you have that method
        System.out.println("Manual notification send: " + req);
        return ResponseEntity.ok().build();
    }

    // ── Mark one read — PUT /api/notifications/{id}/read ─────────────────
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        notifService.markRead(id);
        return ResponseEntity.ok().build();
    }

    // ── Mark all read — PUT /api/notifications/read-all ──────────────────
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        notifService.markAllRead(userId);
        return ResponseEntity.ok().build();
    }

    // ── Disconnect SSE — DELETE /api/notifications/stream ────────────────
    @DeleteMapping("/stream")
    public ResponseEntity<Void> disconnect(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        notifService.removeEmitter(userId);
        return ResponseEntity.ok().build();
    }

    // ── Helper ────────────────────────────────────────────────────────────
    private Long resolveUserId(UserDetails userDetails) {
        return userRepo.findByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}