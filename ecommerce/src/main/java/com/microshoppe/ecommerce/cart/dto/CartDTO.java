package com.microshoppe.ecommerce.cart.dto;

import com.microshoppe.ecommerce.cart.enums.CartStatus;
import com.microshoppe.ecommerce.cart.model.Cart;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CartDTO {
    private Long id;
    private BigDecimal total;
    private String cartCode;
    private String orderCode;
    private Instant createdAt;
    private Instant updatedAt;
    private CartStatus status;
    private List<CartItemDTO> items;
    private BigDecimal deliveryFee;

    public CartDTO(Cart cart) {
        this.id = cart.getId();
        this.total = cart.getTotal();
        this.cartCode = cart.getCartCode();
        this.createdAt = cart.getCreatedAt();
        this.updatedAt = cart.getUpdatedAt();
        this.status = cart.getStatus();
        this.items = cart.getItems().stream()
                .map(CartItemDTO::new)
                .collect(Collectors.toList());
        this.orderCode = cart.getOrderCode();
        this.deliveryFee = cart.getDeliveryFee();
    }
}

