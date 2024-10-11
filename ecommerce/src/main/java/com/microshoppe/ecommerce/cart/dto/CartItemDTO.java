package com.microshoppe.ecommerce.cart.dto;

import com.microshoppe.ecommerce.cart.enums.CartItemStatus;
import com.microshoppe.ecommerce.cart.model.CartItem;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CartItemDTO {
    private Long id;
    private String codeProduct;
    private Long productId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Integer quantity;
    private BigDecimal totalPrice;
    private CartItemStatus status;
    private String imageUrl;
    private List<CartItemOptionDTO> options;
    private String categoryName;
    private String observation;

    public CartItemDTO(CartItem cartItem) {
        this.id = cartItem.getId();
        this.codeProduct = cartItem.getCodeProduct();
        this.productId = cartItem.getProduct().getId();
        this.name = cartItem.getProduct().getName();
        this.description = cartItem.getProduct().getDescription();
        this.price = cartItem.getProduct().getPrice();
        this.stock = cartItem.getProduct().getStock();
        this.quantity = cartItem.getQuantity();
        this.totalPrice = cartItem.getTotalPrice();
        this.status = cartItem.getStatus();
        this.imageUrl = cartItem.getProduct().getImageUrl();
        this.options = cartItem.getOptions().stream()
                .map(CartItemOptionDTO::new)
                .collect(Collectors.toList());
        this.categoryName = cartItem.getProduct().getCategory().getName(); // Exemplo de como adicionar categoria
        this.observation = cartItem.getObservation();
    }
}
