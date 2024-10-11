package com.microshoppe.ecommerce.order.repository;

import com.microshoppe.ecommerce.order.model.OrderItemOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemOptionRepository extends JpaRepository<OrderItemOption, Long> {}