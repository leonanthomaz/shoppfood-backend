package com.microshoppe.ecommerce.payment.repository;

import com.microshoppe.ecommerce.payment.model.LocalPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalPaymentRepository extends JpaRepository<LocalPayment, Long> {}
