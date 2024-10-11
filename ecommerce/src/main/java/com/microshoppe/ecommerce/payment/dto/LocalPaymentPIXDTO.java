package com.microshoppe.ecommerce.payment.dto;

import lombok.Data;

@Data
public class LocalPaymentPIXDTO {
    private String cartCode;
    private String qrCodeUrl;
    private String qrCodeBase64;
    private Long expirationTime;
}
