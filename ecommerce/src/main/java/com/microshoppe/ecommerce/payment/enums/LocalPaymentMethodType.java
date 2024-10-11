package com.microshoppe.ecommerce.payment.enums;

public enum LocalPaymentMethodType {
    CASH("CASH"),
    CREDIT_CARD("CREDIT_CARD"),
    PIX("PIX");

    private final String methodCode;

    LocalPaymentMethodType(String methodCode) {
        this.methodCode = methodCode;
    }

    public String getMethodCode() {
        return methodCode;
    }

    public static LocalPaymentMethodType fromCode(String methodCode) {
        for (LocalPaymentMethodType method : LocalPaymentMethodType.values()) {
            if (method.getMethodCode().equals(methodCode)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Invalid PaymentMethodCode: " + methodCode);
    }
}