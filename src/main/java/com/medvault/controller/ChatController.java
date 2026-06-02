package com.medvault.controller;

import com.medvault.dto.request.ChatRequest;
import com.medvault.dto.response.ChatResponse;
import com.medvault.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /** POST /api/chat — send message, get bot reply */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest req,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(chatService.chat(principal.getUsername(), req));
    }

    /** GET /api/chat/history — get past messages */
    @GetMapping("/history")
    public ResponseEntity<List<ChatResponse>> history(
            @AuthenticationPrincipal UserDetails principal){
        return ResponseEntity.ok(chatService.getHistory(principal.getUsername()));
    }
}