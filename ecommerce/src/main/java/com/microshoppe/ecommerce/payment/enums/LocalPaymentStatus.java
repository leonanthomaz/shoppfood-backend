package com.microshoppe.ecommerce.payment.enums;

public enum LocalPaymentStatus {
    CONFIRMED("CONFIRMED"),
    PENDING("PENDING"),
    REJECTED("REJECTED"),
    AWAINTING_PAYMENT("AWAINTING_PAYMENT"),
    CANCELED("CANCELED"),
    PAID("PAID");

    private final String PaymentCode;

    LocalPaymentStatus(String paymentCode) {
        this.PaymentCode = paymentCode;
    }

    public String getPaymentCode() {
        return PaymentCode;
    }

    public static LocalPaymentStatus fromCode(String paymentCode) {
        for (LocalPaymentStatus status : LocalPaymentStatus.values()) {
            if (status.getPaymentCode().equals(paymentCode)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid PaymentCode: " + paymentCode);
    }
}
