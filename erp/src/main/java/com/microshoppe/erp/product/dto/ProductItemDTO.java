package com.microshoppe.erp.product.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductItemDTO {
    private String name;
    private String merchantCode;
    private BigDecimal additionalPrice;
}
