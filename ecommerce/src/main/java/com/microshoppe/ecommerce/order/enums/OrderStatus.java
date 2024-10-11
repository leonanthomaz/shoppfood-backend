package com.microshoppe.ecommerce.order.enums;

public enum OrderStatus {
    CREATED("CREATED"),
    PENDING("PENDING"),
    PROCESSING("PROCESSING"),
    ACCEPT("ACCEPT"),
    CONFIRMED("CONFIRMED"),
    CANCELLED("CANCELLED"),
    AWAINTING_PAYMENT("AWAINTING_PAYMENT"),
    FAIL_PAID("FAIL_PAID"),
    PAID("PAID"),

    ACCEPTED("ACCEPTED"),
    PREPARING("PREPARING"),
    READY_FOR_DELIVERY("READY_FOR_DELIVERY"),
    OUT_FOR_DELIVERY("OUT_FOR_DELIVERY"),
    DELIVERED("DELIVERED");

    private final String orderCode;

    OrderStatus(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public static OrderStatus fromCode(String orderCode) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.getOrderCode().equals(orderCode)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid orderCode: " + orderCode);
    }
}
