package com.microshoppe.ecommerce.cart.dto;

import com.microshoppe.erp.authentication.dto.UserDetailsDTO;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CheckoutRequestDTO {
    private String merchantCode;
    private String cartCode;
    private String token;
    private UserDetailsDTO user;
    private BigDecimal deliveryFee;
}
