// com/medvault/dto/response/NotificationResponse.java
package com.medvault.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long          id;
    private String        title;
    private String        message;
    private String        type;
    private String        severity;
    private String        actionUrl;
    private Long          refId;
    private boolean       read;
    private LocalDateTime createdAt;
}