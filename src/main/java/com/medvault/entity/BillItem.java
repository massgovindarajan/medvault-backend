//
//package com.medvault.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//
//@Entity
//@Table(name = "bill_items")
//@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
//public class BillItem {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "bill_id", nullable = false)
//    private Bill bill;
//
//    /** CONSULTATION | MEDICINE | LAB | SERVICE | OTHER */
//    @Column(nullable = false, length = 30)
//    private String category;
//
//    @Column(nullable = false, length = 200)
//    private String description;
//
//    @Column(nullable = false)
//    @Builder.Default
//    private Integer quantity = 1;
//
//    @Column(name = "unit_price", nullable = false)
//    private Double unitPrice;
//
//    @Column(name = "total_price", nullable = false)
//    private Double totalPrice;  // quantity × unitPrice
//}
package com.medvault.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bill_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    // CONSULTATION | MEDICINE | LAB | PROCEDURE | ROOM | OTHER
    private String category;

    private String description;

    private Integer quantity;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "total_price")
    private Double totalPrice;
}