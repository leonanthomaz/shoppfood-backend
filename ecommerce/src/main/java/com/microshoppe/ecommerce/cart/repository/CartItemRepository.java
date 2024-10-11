package com.microshoppe.ecommerce.cart.repository;

import com.microshoppe.ecommerce.cart.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    CartItem findByCartIdAndCodeProduct(Long id, String codeProduct);
}
