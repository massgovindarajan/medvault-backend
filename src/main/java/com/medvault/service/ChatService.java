package com.medvault.service;

import com.medvault.dto.request.ChatRequest;
import com.medvault.dto.response.ChatResponse;
import java.util.List;

public interface ChatService {
    ChatResponse       chat(String userEmail, ChatRequest req);
    List<ChatResponse> getHistory(String userEmail);
}