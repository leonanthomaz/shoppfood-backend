package com.microshoppe.ecommerce.cart.repository;

import com.microshoppe.ecommerce.cart.model.CartItemOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemOptionRepository extends JpaRepository<CartItemOption, Long> {
    CartItemOption findByCartItemIdAndCodeOption(Long cartItemId, String codeOption);
}
