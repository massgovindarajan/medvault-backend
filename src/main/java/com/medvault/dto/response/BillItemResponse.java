package com.medvault.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class BillItemResponse {
    private Long    id;
    private String  category;
    private String  description;
    private Integer quantity;
    private Double  unitPrice;
    private Double  totalPrice;
}