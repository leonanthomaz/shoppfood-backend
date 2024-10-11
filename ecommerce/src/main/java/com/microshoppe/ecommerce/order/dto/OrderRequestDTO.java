package com.microshoppe.ecommerce.order.dto;

import lombok.Data;

@Data
public class OrderRequestDTO {
    private String merchantCode;
    private String cartCode;
    private String token;
    private OrderUserDTO user;
}
