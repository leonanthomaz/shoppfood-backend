package com.microshoppe.ecommerce.cart.dto;

import lombok.Data;

@Data
public class ProductChangeRequestDTO {
    private String cartCode;
    private String codeProduct;
    private Long productId;
}
