package com.medvault.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class ChatResponse {
    private String       reply;
    private String       sessionId;
    private List<String> suggestions;
    private String       sentAt;
}
