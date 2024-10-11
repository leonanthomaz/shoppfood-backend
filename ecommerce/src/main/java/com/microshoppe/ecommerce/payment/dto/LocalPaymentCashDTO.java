package com.microshoppe.ecommerce.payment.dto;

import com.microshoppe.ecommerce.payment.enums.LocalPaymentMethodType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LocalPaymentCashDTO {
    private String orderCode;
    private String cartCode;
    private LocalPaymentMethodType paymentMethod;
    private BigDecimal cashChange;
}
