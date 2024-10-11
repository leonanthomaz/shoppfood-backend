package com.microshoppe.ecommerce.cart.enums;

public enum CartStatus {

    CREATED("CREATED"),
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE"),
    REMOVE("REMOVE"),
    EMPTY("EMPTY"),
    CLEAR("CLEAR"),
    DELETE("DELETE"),
    PROCESSING("PROCESSING"),
    CHECKOUT("CHECKOUT"),
    ORDER("ORDER"),
    FINISHED("FINISHED");

    private final String cartCode;

    private CartStatus(String cartCode) {
        this.cartCode = cartCode;
    }

    public String getCartCode() {
        return cartCode;
    }

    public static CartStatus fromCode(String cartCode) {
        for (CartStatus status : CartStatus.values()) {
            if (status.getCartCode().equals(cartCode)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid cartCode: " + cartCode);
    }
}
