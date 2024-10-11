package com.microshoppe.ecommerce.order.repository;

import com.microshoppe.ecommerce.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findById(Long id);
    List<Order> findByUserId(Long userId);
    Optional<Order> findByOrderCode(String orderCode);
    List<Order> findByMerchantCode(String merchantCode);
}
