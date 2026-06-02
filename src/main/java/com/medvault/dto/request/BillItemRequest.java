package com.medvault.dto.request;

import lombok.Data;

@Data
public class BillItemRequest {
    private String  category;     
    private String  description;
    private Integer quantity;
    private Double  unitPrice;
}