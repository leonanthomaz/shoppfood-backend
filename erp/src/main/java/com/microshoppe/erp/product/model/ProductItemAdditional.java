package com.microshoppe.erp.product.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_product_item_additional")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductItemAdditional {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_option_additional", unique = true, nullable = false)
    private String codeOptionAdditional;

    private String merchantCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonBackReference
    private Product product;

    private String name;
    private BigDecimal additionalPrice;
}
