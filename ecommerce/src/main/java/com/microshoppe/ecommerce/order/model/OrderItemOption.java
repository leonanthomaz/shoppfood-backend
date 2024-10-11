package com.microshoppe.ecommerce.order.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_order_item_option")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String merchantCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    @JsonBackReference
    private OrderItem orderItem;

    private String name;
    private int quantity;
}

