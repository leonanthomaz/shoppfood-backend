package com.microshoppe.ecommerce.payment.service;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.microshoppe.ecommerce.payment.dto.LocalPaymentPIXDTO;
import com.microshoppe.ecommerce.payment.dto.LocalPaymentRequestDTO;
import com.microshoppe.ecommerce.payment.exception.LocalPaymentProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
@RequiredArgsConstructor
public class LocalPaymentService {

    private final PaymentClient paymentClient;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public Payment processPayment(LocalPaymentRequestDTO paymentRequest) {
        log.info("PAYMENT >>> DADOS RECEBIDOS - PROCESSAMENTO DE PAGAMENTO: {}", paymentRequest);
        PaymentCreateRequest.PaymentCreateRequestBuilder createRequestBuilder = PaymentCreateRequest.builder()
                .issuerId(paymentRequest.getIssuerId())
                .paymentMethodId(paymentRequest.getPaymentMethodId())
                .transactionAmount(paymentRequest.getTransactionAmount())
                .installments(paymentRequest.getInstallments())
                .payer(PaymentPayerRequest.builder().email(paymentRequest.getPayer().getEmail()).build());
        log.info("PAYMENT >>> DADOS PROCESSADOS: {}", createRequestBuilder);

        if ("pix".equalsIgnoreCase(paymentRequest.getPaymentMethodId())) {
            createRequestBuilder.token(null);
        }

        PaymentCreateRequest createRequest = createRequestBuilder.build();
        log.info("PAYMENT >>> CRIANDO REQUISIÇÃO DE PAGAMENTO: {}", createRequest);

        try {
            Payment payment = paymentClient.create(createRequest);
            log.info("PAYMENT >>> PAGAMENTO CRIADO: {}", payment);
            if ("pix".equalsIgnoreCase(paymentRequest.getPaymentMethodId())) {
                log.info("PAYMENT >>> QR Code URL: {}", payment.getPointOfInteraction().getTransactionData().getQrCode());
                log.info("PAYMENT >>> QR Code Base64: {}", payment.getPointOfInteraction().getTransactionData().getQrCodeBase64());
            }
            log.info("PAYMENT >>> PAGAMENTO FINALIZADO: {}", payment);
            return payment;
        } catch (MPApiException ex) {
            log.error("PAYMENT >>> MercadoPago Error. Status: {}, Content: {}", ex.getApiResponse().getStatusCode(), ex.getApiResponse().getContent());
            throw new LocalPaymentProcessingException("Erro ao processar pagamento: " + ex.getMessage(), ex);
        } catch (MPException ex) {
            log.error("PAYMENT >>> Erro ao processar pagamento", ex);
            throw new LocalPaymentProcessingException("Erro ao processar pagamento: " + ex.getMessage(), ex);
        }
    }

    public Payment generatePixQRCode(LocalPaymentRequestDTO paymentRequest) {
        log.info("PAYMENT >>> DADOS RECEBIDOS - GERANDO PAGAMENTO POR PIX: {}", paymentRequest);
        PaymentCreateRequest.PaymentCreateRequestBuilder createRequestBuilder = PaymentCreateRequest.builder()
                .transactionAmount(paymentRequest.getTransactionAmount())
                .paymentMethodId("pix")
                .payer(PaymentPayerRequest.builder().email(paymentRequest.getPayer().getEmail()).build());
        log.info("PAYMENT >>>  DADOS PROCESSADOS PIX: {}", createRequestBuilder);

        PaymentCreateRequest createRequest = createRequestBuilder.build();
        log.info("PAYMENT >>> CRIANDO REQUISIÇÃO DE PAGAMENTO PIX: {}", createRequest);

        try {
            Payment payment = paymentClient.create(createRequest);
            log.info("PAYMENT >>> PAGAMENTO PIX CRIADO: {}", payment);
            return payment;
        } catch (MPApiException ex) {
            log.error("PAYMENT >>> MercadoPago Error. Status: {}, Content: {}", ex.getApiResponse().getStatusCode(), ex.getApiResponse().getContent());
            throw new LocalPaymentProcessingException("Erro ao processar pagamento: " + ex.getMessage(), ex);
        } catch (MPException ex) {
            log.error("PAYMENT >>> Erro ao processar pagamento", ex);
            throw new LocalPaymentProcessingException("Erro ao processar pagamento: " + ex.getMessage(), ex);
        }
    }

    public LocalPaymentPIXDTO generatePixQRCodeWithExpiration(LocalPaymentRequestDTO paymentRequest, int expirationMinutes) {
        log.info("PAYMENT >>> DADOS RECEBIDOS - GERANDO PAGAMENTO POR PIX COM EXPIRAÇÃO: {}", paymentRequest);

        PaymentCreateRequest.PaymentCreateRequestBuilder createRequestBuilder = PaymentCreateRequest.builder()
                .transactionAmount(paymentRequest.getTransactionAmount())
                .paymentMethodId("pix")
                .payer(PaymentPayerRequest.builder().email(paymentRequest.getPayer().getEmail()).build());

        PaymentCreateRequest createRequest = createRequestBuilder.build();

        try {
            Payment payment = paymentClient.create(createRequest);
            log.info("PAYMENT >>> PAGAMENTO PIX CRIADO: {}", payment);

            // Agendar expiração do QR Code
            scheduler.schedule(() -> handleQrCodeExpiration(payment.getId()), expirationMinutes, TimeUnit.MINUTES);

            LocalPaymentPIXDTO responseDTO = new LocalPaymentPIXDTO();
            responseDTO.setQrCodeUrl(payment.getPointOfInteraction().getTransactionData().getQrCode());
            responseDTO.setQrCodeBase64(payment.getPointOfInteraction().getTransactionData().getQrCodeBase64());
            responseDTO.setExpirationTime(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(expirationMinutes));

            return responseDTO;
        } catch (MPApiException | MPException ex) {
            log.error("PAYMENT >>> Erro ao processar pagamento", ex);
            throw new LocalPaymentProcessingException("Erro ao processar pagamento: " + ex.getMessage(), ex);
        }
    }

    private void handleQrCodeExpiration(Long paymentId) {
        try {
            Payment payment = paymentClient.get(paymentId);
            if (!"approved".equalsIgnoreCase(payment.getStatus())) {
                log.info("PAYMENT >>> Expiração do QR Code. Cancelando pagamento: {}", paymentId);
                // Aqui você pode atualizar o status do pedido
                cancelOrderDueToExpiration(paymentId);
            }
        } catch (MPException ex) {
            log.error("Erro ao verificar status do pagamento para expiração: {}", ex.getMessage());
        } catch (MPApiException e) {
            throw new RuntimeException(e);
        }
    }

    public Payment getPaymentDetails(Long paymentId) {
        try {
            log.info("PAYMENT >>> BUSCANDO DETALHES DO PAGAMENTO PARA ID: {}", paymentId);
            return paymentClient.get(paymentId);
        } catch (MPApiException ex) {
            log.error("PAYMENT >>> Erro da API MercadoPago ao buscar detalhes. Status: {}, Content: {}", ex.getApiResponse().getStatusCode(), ex.getApiResponse().getContent());
            throw new LocalPaymentProcessingException("Erro ao buscar detalhes do pagamento: " + ex.getMessage(), ex);
        } catch (MPException ex) {
            log.error("PAYMENT >>> Erro ao buscar detalhes do pagamento", ex);
            throw new LocalPaymentProcessingException("Erro ao buscar detalhes do pagamento: " + ex.getMessage(), ex);
        }
    }

    private void cancelOrderDueToExpiration(Long paymentId) {
        // Implementar lógica para cancelar o pedido associado ao paymentId
    }
}
