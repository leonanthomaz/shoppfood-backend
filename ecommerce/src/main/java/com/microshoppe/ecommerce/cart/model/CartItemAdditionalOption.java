package com.microshoppe.ecommerce.cart.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_cart_item_additional_option")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemAdditionalOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String merchantCode;

    @Column(name = "code_additional_option", nullable = false)
    private String codeAdditionalOption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_item_id")
    @JsonBackReference
    private CartItem cartItem;

    private String name;
    private BigDecimal price;
    private Integer quantity;

    public Long getAdditionalOptionId() {
        return this.id;
    }
}
