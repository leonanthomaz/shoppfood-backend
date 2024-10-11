package com.microshoppe.ecommerce.cart.enums;

public enum CartItemStatus {

    BLOCKED("BLOCKED"),
    PENDING("PENDING"),
    RELEASED("RELEASED");

    private final String statusCode;

    CartItemStatus(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public static CartItemStatus fromCode(String statusCode) {
        for (CartItemStatus status : CartItemStatus.values()) {
            if (status.getStatusCode().equals(statusCode)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid statusCode: " + statusCode);
    }
}
