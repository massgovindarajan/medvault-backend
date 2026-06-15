package com.medvault.dto.response;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class MedicineInventoryResponse {
    private Long    id;
    private String  medicineName;
    private String  genericName;
    private String  category;
    private Integer stockQty;
    private String  unit;
    private Integer lowStockThreshold;
    private Double  unitPrice;
    private String  manufacturer;
    private Boolean isActive;
    private Boolean isLowStock;      
    private String  updatedAt;
   
    private LocalDate expiryDate;

}