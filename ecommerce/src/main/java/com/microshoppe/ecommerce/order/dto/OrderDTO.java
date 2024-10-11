package com.microshoppe.ecommerce.order.dto;

import com.microshoppe.ecommerce.order.enums.OrderStatus;
import com.microshoppe.ecommerce.order.model.Order;
import com.microshoppe.ecommerce.order.model.OrderItem;
import com.microshoppe.ecommerce.order.model.OrderItemOption;
import com.microshoppe.ecommerce.payment.enums.LocalPaymentMethodType;
import com.microshoppe.erp.authentication.model.User;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderDTO {
    private Long id;
    private String cartCode;
    private String orderCode;
    private String merchantCode;
    private BigDecimal total;
    private Instant createdAt;
    private Instant updatedAt;
    private OrderStatus status;
    private List<OrderItemDTO> items;
    private BigDecimal deliveryFee;

    private User user;
    private LocalPaymentMethodType paymentMethod;
    private BigDecimal cashChange;

    public OrderDTO(Order order) {
        this.id = order.getId();
        this.orderCode = order.getOrderCode();
        this.merchantCode = order.getMerchantCode();
        this.total = order.getTotal();
        this.createdAt = order.getCreatedAt();
        this.updatedAt = order.getUpdatedAt();
        this.status = order.getStatus();
        this.user = order.getUser();
        this.items = order.getItems().stream()
                .map(OrderItemDTO::new)
                .collect(Collectors.toList());
        this.paymentMethod = order.getPaymentMethod();
        this.cashChange = order.getCashChange();
        this.cartCode = order.getCartCode();
        this.deliveryFee = order.getDeliveryFee();
    }

    @Data
    public static class OrderItemDTO {
        private Long id;
        private Long productId;
        private int quantity;
        private String productName;
        private BigDecimal productPrice;
        private List<OrderItemOptionDTO> options;

        public OrderItemDTO(OrderItem orderItem) {
            this.id = orderItem.getId();
            this.productId = orderItem.getProduct().getId();
            this.quantity = orderItem.getQuantity();
            this.productName = orderItem.getProduct().getName();
            this.productPrice = orderItem.getProduct().getPrice();
            this.options = (orderItem.getOptions() != null ? orderItem.getOptions().stream()
                    .map(OrderItemOptionDTO::new)
                    .collect(Collectors.toList()) : new ArrayList<>());
        }
    }

    @Data
    public static class OrderItemOptionDTO {
        private Long id;
        private String name;
        private Integer quantity;

        public OrderItemOptionDTO(OrderItemOption orderItemOption) {
            this.id = orderItemOption.getId();
            this.name = orderItemOption.getName();
            this.quantity = orderItemOption.getQuantity();
        }
    }
}
