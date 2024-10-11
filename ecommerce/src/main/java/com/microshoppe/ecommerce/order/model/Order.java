package com.microshoppe.ecommerce.order.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.microshoppe.ecommerce.order.enums.OrderStatus;
import com.microshoppe.ecommerce.payment.enums.LocalPaymentMethodType;
import com.microshoppe.erp.authentication.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "tb_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String cartCode;
    private String orderCode;
    private String merchantCode;
    private Long paymentId;;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonManagedReference
    @ToString.Exclude
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    @ToString.Exclude
    private List<OrderItem> items;

    private BigDecimal total;
    private BigDecimal deliveryFee;

    private Instant createdAt;
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    // Novo campo para método de pagamento
    @Enumerated(EnumType.STRING)
    private LocalPaymentMethodType paymentMethod; // Usando o enum que você criar

    @Column(precision = 10, scale = 2)
    private BigDecimal cashChange; // Novo campo para troco
}
