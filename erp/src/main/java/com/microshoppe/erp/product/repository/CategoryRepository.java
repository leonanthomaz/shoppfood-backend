package com.microshoppe.erp.product.repository;

import com.microshoppe.erp.product.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByNameContaining(String name);
    List<Category> findByMerchantCode(String merchantCode);
}
