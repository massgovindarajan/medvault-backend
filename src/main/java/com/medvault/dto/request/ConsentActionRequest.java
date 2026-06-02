package com.medvault.dto.request;

import lombok.Data;

@Data
public class ConsentActionRequest {
    private String action;   // "APPROVE" or "DENY"
}