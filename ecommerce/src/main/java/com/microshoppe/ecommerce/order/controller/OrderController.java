package com.microshoppe.ecommerce.order.controller;

import com.microshoppe.ecommerce.order.dto.OrderDTO;
import com.microshoppe.ecommerce.order.dto.TesteDTO;
import com.microshoppe.ecommerce.order.model.Order;
import com.microshoppe.ecommerce.order.service.OrderService;
import com.microshoppe.erp.product.model.Product;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    @Value("${twilio.account.sid}")
    private String twilio_account;

    @Value("${twilio.auth.token}")
    private String twilio_token;

    @Value("${twilio.phone.number}")
    private String twilio_phonenumber;

    private final OrderService orderService;

    @PostMapping("/teste")
    public ResponseEntity<String> teste(@RequestBody @Valid TesteDTO data) {
        Twilio.init(twilio_account, twilio_token);

        try {
            Message message = Message.creator(
                            new com.twilio.type.PhoneNumber("whatsapp:+55"+data.getPhoneNumber()),
                            new com.twilio.type.PhoneNumber("whatsapp:"+twilio_phonenumber),
                            data.getOrderDetails())
                    .create();

            System.out.println(message.getSid());
            return ResponseEntity.status(HttpStatus.OK).body("");
        } catch (Exception e) {
            throw new RuntimeException("");
        }
    }

    @PostMapping("/cancel/{orderCode}")
    public ResponseEntity<?> cancelOrderAndReturnsCart(@PathVariable String orderCode){
        try{
            Order order = orderService.cancelOrder(orderCode);
            return ResponseEntity.status(HttpStatus.OK).body(order.getCartCode());
        } catch (RuntimeException error){
            throw new RuntimeException("FALHA AO PROCESSAR CANCELAMENTO DO PEDIDO E REATIVAÇÃO DO CARRINHO");
        }
    }

    @GetMapping
    public ResponseEntity<List<Order>> findAll(){
        return ResponseEntity.status(HttpStatus.OK).body(orderService.findAll());
    }

    @GetMapping("/find/{orderCode}")
    public ResponseEntity<OrderDTO> getOrderByOrderCode(@PathVariable String orderCode) {
        log.info("ORDER >>> DADOS RECUPERADOS VIA REQUISIÇÃO - orderCode: {}", orderCode);
        try {
            OrderDTO order = orderService.getOrderByOrderCode(orderCode);
            log.info("ORDER >>> DADOS RECUPERADOS VIA REQUISIÇÃO - PEDIDO POR CÓDIGO: {}", orderCode);
            return ResponseEntity.status(HttpStatus.OK).body(order);
        } catch (RuntimeException e) {
            log.error("ORDER >>> FALHA AO BUSCAR PEDIDO: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/find/merchant/{merchantCode}")
    public ResponseEntity<List<Order>> getOrderByMerchantCode(@PathVariable String merchantCode) {
        log.info("ORDER >>> DADOS RECUPERADOS VIA REQUISIÇÃO - merchantCode: {}", merchantCode);
        try {
            List<Order> orders = orderService.getByMerchantCode(merchantCode);
            if (orders.isEmpty()) {
                log.info("ORDER >>> NENHUM PEDIDO ENCONTRADO PARA O CÓDIGO DO COMERCIANTE: {}", merchantCode);
                return ResponseEntity.ok(orders); // Retorna 200 OK com lista vazia
            }
            log.info("ORDER >>> DADOS RECUPERADOS VIA REQUISIÇÃO - PEDIDOS ENCONTRADOS PARA O CÓDIGO: {}", merchantCode);
            return ResponseEntity.ok(orders);
        } catch (RuntimeException e) {
            log.error("ORDER >>> FALHA AO BUSCAR PEDIDOS: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Retorna 500 em caso de erro
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        log.info("ORDER >>> DADOS RECUPERADOS VIA REQUISIÇÃO - ID: {}", id);
        try {
            OrderDTO orderDTO = orderService.getOrderById(id);
            log.info("ORDER >>> DADOS RECUPERADOS VIA REQUISIÇÃO - PEDIDO POR ID: {}", orderDTO);
            return ResponseEntity.status(HttpStatus.OK).body(orderDTO);
        } catch (RuntimeException e) {
            log.error("Erro ao buscar pedido: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDTO>> getOrderByUserId(@PathVariable Long userId) {
        log.info("ORDER (GET BY USERID) - DADOS RECUPERADOS VIA REQUISIÇÃO - getOrderByUserId: {}", userId);
        try {
            List<OrderDTO> ordersDTO = orderService.getOrderByUserId(userId);
            log.info("ORDER >>> DADOS RECUPERADOS VIA REQUISIÇÃO - PEDIDO POR ID DO USUÁRIO: {}", ordersDTO);
            return ResponseEntity.status(HttpStatus.OK).body(ordersDTO);
        } catch (RuntimeException e) {
            log.error("FALHA AO BUSCAR PEDIDOS: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/bestsellers")
    public ResponseEntity<List<Product>> getBestSellers() {
        try {
            log.info("ORDER - PRODUCT >>> bestSellers");
            List<Product> bestSellers = orderService.getBestSellers();
            log.info("ORDER - PRODUCT >>> DEVOLVENDO UMA LISTA DE bestSellers");
            return ResponseEntity.ok(bestSellers);
        } catch (RuntimeException error) {
            log.error("ORDER - PRODUCT >>> bestSellers: {}", error.getMessage());
            return ResponseEntity.badRequest().body(null); // Retornando uma resposta adequada
        }
    }


}
