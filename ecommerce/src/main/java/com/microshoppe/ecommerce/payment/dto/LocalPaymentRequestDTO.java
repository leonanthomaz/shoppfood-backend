package com.microshoppe.ecommerce.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalPaymentRequestDTO {
    private String token;
    private String orderCode;
    private String cartCode;
    private String issuerId;
    private String paymentMethodId;
    private BigDecimal transactionAmount;
    private int installments;
    private Payer payer;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payer {
        private String email;
        private Identification identification;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Identification {
            private String type;
            private String number;
        }
    }
}
