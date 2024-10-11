package com.microshoppe.ecommerce.payment.controller;

import com.mercadopago.resources.payment.Payment;
import com.microshoppe.ecommerce.cart.enums.CartStatus;
import com.microshoppe.ecommerce.cart.repository.CartRepository;
import com.microshoppe.ecommerce.cart.service.CartService;
import com.microshoppe.ecommerce.order.dto.OrderDTO;
import com.microshoppe.ecommerce.order.enums.OrderStatus;
import com.microshoppe.ecommerce.order.model.Order;
import com.microshoppe.ecommerce.order.repository.OrderRepository;
import com.microshoppe.ecommerce.order.service.OrderService;
import com.microshoppe.ecommerce.payment.dto.LocalPaymentCashDTO;
import com.microshoppe.ecommerce.payment.dto.LocalPaymentOrderDTO;
import com.microshoppe.ecommerce.payment.dto.LocalPaymentPIXDTO;
import com.microshoppe.ecommerce.payment.dto.LocalPaymentRequestDTO;

import com.microshoppe.ecommerce.payment.enums.LocalPaymentMethodType;
import com.microshoppe.ecommerce.payment.enums.LocalPaymentStatus;
import com.microshoppe.ecommerce.payment.service.LocalPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
public class LocalPaymentController {

    private final LocalPaymentService localPaymentService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final CartService cartService;

    @PostMapping("/card")
    public ResponseEntity<?> processPaymentCardWithLogin(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody @Valid LocalPaymentOrderDTO data) {
        log.info("PAYMENT >>> CARTÃO >>> *** ENTRADA ***");
        try {
            log.info("PAYMENT >>> CARTÃO >>> DADOS RECEBIDOS: {}", data);
            LocalPaymentRequestDTO localPaymentRequestDTO = data.getPaymentDetails();
            LocalPaymentMethodType paymentMethod = data.getPaymentMethod();

            log.info("PAYMENT >>> CARTÃO >>> PROCESSAMENTO ONLINE: {}", localPaymentRequestDTO);
            Payment payment = localPaymentService.processPayment(localPaymentRequestDTO);

            // Verificar se o pedido existe
            Order order = orderRepository.findByOrderCode(data.getOrderCode())
                    .orElseThrow(() -> new RuntimeException("PAYMENT >>> PIX >>> Pedido não encontrado"));

            if ("approved".equalsIgnoreCase(payment.getStatus())) {
                order.setStatus(OrderStatus.PAID);
                order.setPaymentMethod(paymentMethod);
                cartService.updateStatusCart(CartStatus.FINISHED, data.getOrderCode());
                orderRepository.save(order);
                log.info("PAYMENT >>> CARTÃO >>> PAGAMENTO APROVADO: {}", order);
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(payment);
            } else {
                order.setStatus(OrderStatus.FAIL_PAID);
                log.info("PAYMENT >>> CARTÃO >>> PAGAMENTO NÃO APROVADO: {}", order);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (RuntimeException e) {
            log.error("PAYMENT >>> CARTÃO >>> FALHA AO PROCESSAR PAGAMENTO NO CARTÃO: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/pix/qr-code")
    public ResponseEntity<?> generatePixQRCode(@RequestBody @Valid LocalPaymentRequestDTO data) {
        try {
            log.info("PAYMENT >>> PIX >>> DADOS RECEBIDOS => {}", data);
            if (data == null || data.getPayer() == null) {
//                log.warn("PAYMENT >>> Email do pagador está vazio, mas é opcional para pagamentos anônimos.");
                return ResponseEntity.badRequest().body("PAYMENT >>> PIX >>>  Dados do pagamento estão incompletos.");
            }

            // Chame o método e obtenha um PaymentPIXDTO
            LocalPaymentPIXDTO responseDTO = localPaymentService.generatePixQRCodeWithExpiration(data, 10);
//            Payment responseDTO = paymentService.generatePixQRCode(data);
            log.info("PAYMENT >>> PIX >>> DADOS DO PAGAMENTO - GERANDO PIX - RESULTADO: {}", responseDTO);

            Order order = orderRepository.findByOrderCode(data.getOrderCode())
                    .orElseThrow(() -> new RuntimeException("PAYMENT >>> PIX >>> Pedido não encontrado"));

            cartService.updateStatusCart(CartStatus.FINISHED, data.getCartCode());
            order.setStatus(OrderStatus.AWAINTING_PAYMENT);
            orderRepository.save(order);

            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            log.error("PAYMENT >>> Erro ao gerar QR Code Pix: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("PAYMENT >>> Erro ao gerar QR Code Pix");
        }
    }

    @PostMapping("/cash")
    public ResponseEntity<OrderDTO> processPaymentInCash(@RequestBody @Valid LocalPaymentCashDTO data) {
        try {
            log.info("PAYMENT >>> DINHEIRO >>> DADOS RECEBIDOS => {}", data);
            // Verificar se o pedido existe
            Order order = orderRepository.findByOrderCode(data.getOrderCode())
                    .orElseThrow(() -> new RuntimeException("PAYMENT >>> DINHEIRO >>> Pedido não encontrado"));
            // Atualizar status e informações de pagamento do pedido
            order.setStatus(OrderStatus.AWAINTING_PAYMENT);
            order.setPaymentMethod(data.getPaymentMethod());
            order.setCashChange(data.getCashChange());
            cartService.updateStatusCart(CartStatus.FINISHED, data.getCartCode());
            // Salvar alterações
            Order savedOrder = orderRepository.save(order);

            log.info("PAYMENT >>> DINHEIRO >>> ATUALIZAÇÃO DE PEDIDO => {}", savedOrder);
            return ResponseEntity.status(HttpStatus.OK).body(new OrderDTO(savedOrder));
        } catch (RuntimeException error) {
            log.error("PAYMENT >>> DINHEIRO >>> FALHA AO PROCESSAR PAGAMENTO EM DINHEIRO", error);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/pix/anonymous")
    public ResponseEntity<?> processPaymentPixWithoutLogin(@RequestBody @Valid LocalPaymentOrderDTO data){
        log.info("PAYMENT >>> PIX SEM LOGIN >>> *** ENTRADA ***");
        try{
            log.info("PAYMENT >>> RECEBENDO DADOS SEM PAGAMENTO ONLINE: {}", data);

            // Verificar se o pedido existe
            Order order = orderRepository.findByOrderCode(data.getOrderCode())
                    .orElseThrow(() -> new RuntimeException("PAYMENT >>> PIX SEM LOGIN >>> Pedido não encontrado"));

            // Atualizar status e informações de pagamento do pedido
            order.setStatus(OrderStatus.AWAINTING_PAYMENT);
            order.setPaymentMethod(data.getPaymentMethod());

            // Salvar alterações
            Order savedOrder = orderRepository.save(order);

            cartService.updateStatusCart(CartStatus.FINISHED, data.getCartCode());

            log.info("PAYMENT >>> PIX SEM LOGIN >>> ATUALIZAÇÃO DE PEDIDO => {}", savedOrder);
            return ResponseEntity.status(HttpStatus.OK).body(new OrderDTO(savedOrder));

        } catch (Exception e) {
            log.error("PAYMENT >>> PIX SEM LOGIN >>> Erro ao processar Pix: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("PAYMENT >>> Erro ao processar Pix");
        }
    }

    @PostMapping("/card/anonymous")
    public ResponseEntity<?> processPaymentCardWithoutLogin(@RequestBody @Valid LocalPaymentOrderDTO data){
        log.info("PAYMENT >>> CARTÃO SEM LOGIN >>> *** ENTRADA ***");
        try{
            log.info("PAYMENT >>> RECEBENDO DADOS SEM PAGAMENTO ONLINE: {}", data);
            // Verificar se o pedido existe
            Order order = orderRepository.findByOrderCode(data.getOrderCode())
                    .orElseThrow(() -> new RuntimeException("PAYMENT >>> CARTÃO SEM LOGIN >>> Pedido não encontrado"));

            // Atualizar status e informações de pagamento do pedido
            order.setStatus(OrderStatus.AWAINTING_PAYMENT);
            order.setPaymentMethod(data.getPaymentMethod());

            cartService.updateStatusCart(CartStatus.FINISHED, data.getCartCode());

            // Salvar alterações
            orderRepository.save(order);
            log.info("PAYMENT >>> CARTÃO >>> SEM TOKEN, APENAS ATUALIZA O PEDIDO: {}", order);
            return ResponseEntity.ok("Dados recebidos. O pagamento será feito presencialmente.");
        } catch (Exception e) {
            log.error("PAYMENT >>> Erro ao processar Cartão: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("PAYMENT >>> Erro ao processar Cartão");
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handlePaymentNotification(@RequestBody Map<String, Object> notification) {
        log.info("PAYMENT >>> NOTIFICAÇÃO RECEBIDA: {}", notification);

        try {
            String paymentId = notification.get("data").toString();
            Payment payment = localPaymentService.getPaymentDetails(Long.parseLong(paymentId));

            if ("approved".equalsIgnoreCase(payment.getStatus())) {
                log.info("PAYMENT >>> Pagamento aprovado para o ID: {}", paymentId);
                // Atualizar status do pedido para PAGO
                orderService.updateOrderStatusAfterPayment(paymentId, LocalPaymentStatus.PAID);
            } else if ("pending".equalsIgnoreCase(payment.getStatus())) {
                log.info("PAYMENT >>> Pagamento pendente para o ID: {}", paymentId);
                // Atualizar status do pedido para PENDENTE
                orderService.updateOrderStatusAfterPayment(paymentId, LocalPaymentStatus.PENDING);
            } else if ("rejected".equalsIgnoreCase(payment.getStatus())) {
                log.info("PAYMENT >>> Pagamento rejeitado para o ID: {}", paymentId);
                // Atualizar status do pedido para REJEITADO
                orderService.updateOrderStatusAfterPayment(paymentId, LocalPaymentStatus.REJECTED);
            }

            return ResponseEntity.ok("Notificação processada com sucesso");
        } catch (Exception e) {
            log.error("PAYMENT >>> Erro ao processar notificação: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
