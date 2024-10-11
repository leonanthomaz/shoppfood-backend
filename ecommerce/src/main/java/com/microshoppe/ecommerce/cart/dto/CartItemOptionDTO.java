package com.microshoppe.ecommerce.cart.dto;

import com.microshoppe.ecommerce.cart.model.CartItemOption;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemOptionDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private String codeOption;

    public CartItemOptionDTO(CartItemOption option) {
        this.id = option.getId();
        this.name = option.getName();
        this.price = option.getPrice();
        this.quantity = option.getQuantity();
        this.codeOption = option.getCodeOption();
    }
}
