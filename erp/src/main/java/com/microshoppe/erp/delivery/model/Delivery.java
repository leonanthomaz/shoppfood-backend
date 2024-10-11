package com.microshoppe.erp.delivery.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.microshoppe.erp.store.model.Store;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_delivery")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String merchantCode;

    private String cep;
    private double radius;

    private double centralPointLat;
    private double centralPointLng;

    // Novo atributo para a taxa de entrega padrão
    private BigDecimal defaultDeliveryFee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    @JsonBackReference(value = "delivery-store")
    @ToString.Exclude
    private Store store;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    private List<DeliveryZone> zones = new ArrayList<>();
}
