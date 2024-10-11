package com.microshoppe.ecommerce.payment.dto;

import com.microshoppe.ecommerce.payment.enums.LocalPaymentMethodType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalPaymentOrderDTO {
    private String orderCode;
    private String cartCode;
    private String merchantCode;
    private String userToken;
    private LocalPaymentMethodType paymentMethod;
    private LocalPaymentRequestDTO paymentDetails;
}
