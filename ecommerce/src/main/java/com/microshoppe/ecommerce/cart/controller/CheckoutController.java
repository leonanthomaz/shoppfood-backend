package com.microshoppe.ecommerce.cart.controller;

import com.microshoppe.ecommerce.cart.dto.CheckoutRequestDTO;
import com.microshoppe.ecommerce.cart.service.CheckoutService;
import com.microshoppe.ecommerce.order.dto.OrderDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping("/send")
    public ResponseEntity<OrderDTO> sendOrderDetails(@RequestBody CheckoutRequestDTO data) {
        log.info("CHECKOUT >>> DADOS RECEBIDOS DA REQUISIÇÃO: {}", data);
        try {
            OrderDTO order = checkoutService.processCheckout(data);
            log.info("CHECKOUT >>> DADOS DE RETORNO - CHECKOUT PROCESSADO: {}", order);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(order);
        } catch (RuntimeException error) {
            log.error("CHECKOUT >>>  Erro ao processar checkout: {}", error.getMessage());
            throw new RuntimeException("ERRO AO PROCESSAR CHECKOUT");
        }
    }
}