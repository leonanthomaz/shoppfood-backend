package com.microshoppe.ecommerce.cart.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.microshoppe.ecommerce.cart.enums.CartStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_cart")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = true)
    private String cartCode;

    private String orderCode;

    @Column(nullable = false)
    private String merchantCode;

    @Enumerated(EnumType.STRING)
    private CartStatus status;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    @ToString.Exclude
    private List<CartItem> items = new ArrayList<>();

    @Column(nullable = true)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(nullable = true)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    private Instant updatedAt;

    private Instant createdAt;

    public void calculateTotal() {
        this.total = this.items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(this.deliveryFee != null ? this.deliveryFee : BigDecimal.ZERO); // Soma a taxa de entrega
    }

}
