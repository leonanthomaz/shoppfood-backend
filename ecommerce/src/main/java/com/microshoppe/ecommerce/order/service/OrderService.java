package com.microshoppe.ecommerce.order.service;

import com.microshoppe.ecommerce.cart.enums.CartStatus;
import com.microshoppe.ecommerce.cart.model.Cart;
import com.microshoppe.ecommerce.cart.repository.CartRepository;
import com.microshoppe.ecommerce.order.dto.OrderDTO;
import com.microshoppe.ecommerce.order.enums.OrderStatus;
import com.microshoppe.ecommerce.order.model.Order;
import com.microshoppe.ecommerce.order.model.OrderItem;
import com.microshoppe.ecommerce.order.model.OrderItemOption;
import com.microshoppe.ecommerce.order.repository.OrderItemOptionRepository;
import com.microshoppe.ecommerce.order.repository.OrderItemRepository;
import com.microshoppe.ecommerce.order.repository.OrderRepository;
import com.microshoppe.ecommerce.payment.enums.LocalPaymentStatus;
import com.microshoppe.ecommerce.payment.model.LocalPayment;
import com.microshoppe.ecommerce.payment.repository.LocalPaymentRepository;
import com.microshoppe.erp.authentication.model.User;
import com.microshoppe.erp.product.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemOptionRepository orderItemOptionRepository;
    private final CartRepository cartRepository;
    private final LocalPaymentRepository localPaymentRepository;

    private String generateRandomOrderCode() {
        String uuid = UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "");
        return uuid.substring(0, Math.min(uuid.length(), 12)).toUpperCase();
    }

    public OrderDTO createOrder(Cart cart, User user, BigDecimal deliveryFee) {
        log.info("ORDER SERVICE >>> DADOS RECUPERADOS VIA REQUISIÇÃO - : CART={} - USER={}", cart, user);

        if (cart.getStatus() == CartStatus.FINISHED) {
            throw new RuntimeException("ORDER SERVICE >>> Carrinho já foi finalizado.");
        }

        String orderCode = generateRandomOrderCode();
        log.info("ORDER SERVICE >>> CÓDIGO GERADO: {}", orderCode);

        Order order = Order.builder()
                .cartCode(cart.getCartCode())
                .orderCode(orderCode)
                .merchantCode(cart.getMerchantCode())
                .user(user)
                .status(OrderStatus.CREATED)
                .createdAt(Instant.now())
                .total(cart.getTotal())
                .deliveryFee(deliveryFee) // Adicione a taxa de entrega na classe Order se necessário
                .build();
        orderRepository.save(order);
        log.info("ORDER SERVICE >>> PEDIDO GERADO: {}", order);

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> {
                    List<OrderItemOption> orderItemOptions = cartItem.getOptions().stream()
                            .map(cartItemOption -> OrderItemOption.builder()
                                    .name(cartItemOption.getName())
                                    .quantity(cartItemOption.getQuantity())
                                    .build())
                            .collect(Collectors.toList());

                    log.info("ORDER SERVICE >>> ITENS DO CARRINHO: {}", cartItem);
                    log.info("ORDER SERVICE >>> OPÇÕES DO ITEM: {}", orderItemOptions);

                    return OrderItem.builder()
                            .order(order)
                            .product(cartItem.getProduct())
                            .quantity(cartItem.getQuantity())
                            .options(orderItemOptions)
                            .build();
                })
                .collect(Collectors.toList());
        log.info("ORDER SERVICE >>> PRODUTO(S) DO CARRINHO RECUPERADOS: {}", orderItems);

        order.setItems(orderItems);
        Order savedOrder = orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);
        orderItems.forEach(orderItem -> orderItemOptionRepository.saveAll(orderItem.getOptions()));

        log.info("ORDER SERVICE >>> PEDIDO SALVO: {}", savedOrder);

        cart.setStatus(CartStatus.CHECKOUT);
        order.setStatus(OrderStatus.PROCESSING);
        log.info("ORDER SERVICE >>> CARRINHO FINALIZADO: {}", cart);
        log.info("ORDER SERVICE >>> ORDER A ESPERA DE PAGAMENTO: {}", order);

        initiatePayment(order);
        savedOrder = orderRepository.save(order);
        return new OrderDTO(savedOrder);
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public List<Order> getByMerchantCode(String merchantCode) {
        return orderRepository.findByMerchantCode(merchantCode);
    }

    public OrderDTO getOrderByOrderCode(String orderCode) {
        log.info("ORDER SERVICE >>> CODIGO DO PEDIDO RECEBIDO PRA VERIFICAÇÃO : {}", orderCode);
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com o código: " + orderCode));
        log.info("ORDER SERVICE >>> PEDIDO ENCONTRADO POR CÓDIGO : {}", order);
        return new OrderDTO(order);
    }

    public OrderDTO getOrderById(Long id) {
        log.info("ORDER SERVICE >>> ID DO PEDIDO RECEBIDO PRA VERIFICAÇÃO : {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com o ID: " + id));
        log.info("ORDER SERVICE >>> PEDIDO ENCONTRADO POR ID : {}", order);
        return new OrderDTO(order);
    }

    public List<OrderDTO> getOrderByUserId(Long userId) {
        log.info("ORDER SERVICE >>> ID DO PEDIDO RECEBIDO PRA VERIFICAÇÃO : {}", userId);
        List<Order> orders = orderRepository.findByUserId(userId);
        log.info("ORDER SERVICE >>> PEDIDOS ENCONTRADOS POR CÓDIGO DO USUARIO : {}", orders);
        return orders.stream().map(OrderDTO::new).toList();
    }

    public List<Product> getBestSellers() {
        // Buscar todos os pedidos
        List<Order> orders = findAll();

        if (orders.isEmpty()) {
            log.warn("Nenhum pedido encontrado.");
            return Collections.emptyList();
        }

        // Mapa para armazenar a quantidade vendida de cada produto
        Map<Product, Long> productSalesCount = new HashMap<>();

        // Percorrer os itens de cada pedido e acumular as quantidades
        for (Order order : orders) {
            log.info("Processando pedido ID: {}", order.getId());

            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product == null) {
                    log.warn("Item de pedido sem produto associado, pedido ID: {}", order.getId());
                    continue;
                }

                long quantity = item.getQuantity();
                log.info("Adicionando produto: {} com quantidade: {}", product.getName(), quantity);
                productSalesCount.put(product, productSalesCount.getOrDefault(product, 0L) + quantity);
            }
        }

        // Ordenar os produtos pela quantidade vendida em ordem decrescente e pegar os 10 primeiros
        List<Product> bestSellers = productSalesCount.entrySet().stream()
                .sorted(Map.Entry.<Product, Long>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (bestSellers.isEmpty()) {
            log.warn("Nenhum produto mais vendido encontrado.");
        }

        return bestSellers;
    }

    public void initiatePayment(Order order) {
        try {
            LocalPayment localPayment = new LocalPayment();
            localPayment.setTransactionAmount(order.getTotal());
            localPayment.setDescription("Order #" + order.getId());
            localPaymentRepository.save(localPayment);
            order.setPaymentId(localPayment.getId());
            orderRepository.save(order);

        } catch (Exception e) {
            log.error("Erro ao processar pagamento para o pedido {}", order.getId(), e);
        }
    }

    public Order cancelOrder(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode).orElseThrow(() -> new RuntimeException("Pedido não encontrado."));
        if (order == null || order.getStatus() != OrderStatus.PROCESSING) {
            throw new RuntimeException("Pedido não encontrado ou não pode ser cancelado.");
        }
        order.setStatus(OrderStatus.CANCELLED);
        Cart cart = cartRepository.findByCartCode(order.getCartCode()).orElseThrow(() -> new RuntimeException("Carrinho não encontrado."));
        cart.setOrderCode(orderCode);
        cart.setStatus(CartStatus.ACTIVE);
        cartRepository.save(cart);
        orderRepository.save(order);
        return order;
    }

    public void updateOrderStatusAfterPayment(String orderCode, LocalPaymentStatus localPaymentStatus) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        if (localPaymentStatus == LocalPaymentStatus.PAID) {
            order.setStatus(OrderStatus.PAID);
        } else if (localPaymentStatus == LocalPaymentStatus.AWAINTING_PAYMENT) {
            order.setStatus(OrderStatus.AWAINTING_PAYMENT);
        }

        orderRepository.save(order);
        new OrderDTO(order);
    }

}
