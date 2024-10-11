package com.microshoppe.ecommerce.payment.exception;

public class LocalPaymentProcessingException extends RuntimeException {
    public LocalPaymentProcessingException(String message) {
        super(message);
    }
    public LocalPaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

