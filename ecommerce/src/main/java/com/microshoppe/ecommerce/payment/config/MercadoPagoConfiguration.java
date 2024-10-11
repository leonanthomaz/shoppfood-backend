package com.microshoppe.ecommerce.payment.config;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MercadoPagoConfiguration {

    @Value("${mercado_pago.access_token}")
    private String accessToken;

    @Bean
    public PaymentClient paymentClient() {
        MercadoPagoConfig.setAccessToken(accessToken);
        return new PaymentClient();
    }
}

