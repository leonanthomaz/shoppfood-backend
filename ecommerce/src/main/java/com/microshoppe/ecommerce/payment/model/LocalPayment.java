package com.microshoppe.ecommerce.payment.model;

import com.microshoppe.ecommerce.payment.enums.LocalPaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_payment")
public class LocalPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalPaymentStatus status;

    private String description;
    private BigDecimal transactionAmount;

    private Instant createdAt;
    private Instant updatedAt;

    @Column(name = "order_id")
    private Long orderId;
}
