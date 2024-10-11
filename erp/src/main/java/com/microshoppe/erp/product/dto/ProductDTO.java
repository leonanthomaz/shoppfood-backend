package com.microshoppe.erp.product.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class ProductDTO {
    private Long categoryId;
    private String name;
    private String merchantCode;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private String codeBar;
    private Integer getMinimumRequiredOptions;
    private Integer stock;
    private Instant createdAt;
    private Instant updatedAt;
}
