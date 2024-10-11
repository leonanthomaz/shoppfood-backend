package com.microshoppe.ecommerce.cart.dto;

import lombok.Data;

@Data
public class OptionChangeRequestDTO {
    private String cartCode;
    private String codeProduct;
    private String codeOption;
}
