package com.medvault.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Medicine stock managed by the Pharmacist.
 * stockQty is decremented each time a medicine is dispensed.
 */
@Entity
@Table(name = "medicine_inventory")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicineInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "medicine_name", nullable = false, unique = true, length = 200)
    private String medicineName;

    @Column(name = "generic_name", length = 200)
    private String genericName;

    @Column(length = 100)
    private String category;     // e.g. Antibiotic, Painkiller, Antihistamine

    @Column(name = "stock_qty", nullable = false)
    @Builder.Default
    private Integer stockQty = 0;

    @Column(length = 30)
    @Builder.Default
    private String unit = "Tablet"; // Tablet, Capsule, Syrup (ml), Injection

    @Column(name = "low_stock_threshold", nullable = false)
    @Builder.Default
    private Integer lowStockThreshold = 10;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(length = 200)
    private String manufacturer;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Computed: true when stockQty <= lowStockThreshold */
    @Transient
    public boolean isLowStock() { return stockQty <= lowStockThreshold; }
}