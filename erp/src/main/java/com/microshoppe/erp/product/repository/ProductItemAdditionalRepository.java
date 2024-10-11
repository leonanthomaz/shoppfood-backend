package com.microshoppe.erp.product.repository;

import com.microshoppe.erp.product.model.ProductItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductItemAdditionalRepository extends JpaRepository<ProductItem, Long> {}
