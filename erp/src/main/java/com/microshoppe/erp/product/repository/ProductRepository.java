package com.microshoppe.erp.product.repository;

import com.microshoppe.erp.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByMerchantCode(String merchantCode);
    Optional<Product> findById(Long id);
    Optional<Product> findByCodeProduct(String codeProduct);
}
