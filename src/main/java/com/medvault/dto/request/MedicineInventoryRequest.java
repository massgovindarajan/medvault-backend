package com.medvault.dto.request;

import java.time.LocalDate;

import lombok.Data;

@Data
public class MedicineInventoryRequest {
    private String  medicineName;
    private String  genericName;
    private String  category;
    private Integer stockQty;
    private String  unit;
    private Integer lowStockThreshold;
    private Double  unitPrice;
    private String  manufacturer;
    private LocalDate expiryDate;
}