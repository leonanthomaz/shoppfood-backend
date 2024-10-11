package com.microshoppe.erp.delivery.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.microshoppe.erp.store.model.Store;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_delivery_zone")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryZone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String merchantCode;

    private String name;
    private Double price;
    private Double lat;
    private Double lng;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = true)
    @JsonBackReference
    @ToString.Exclude
    private Store store;

    // Adicione esta linha
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    private Delivery delivery;
}
