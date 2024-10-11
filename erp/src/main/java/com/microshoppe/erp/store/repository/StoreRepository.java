package com.microshoppe.erp.store.repository;

import com.microshoppe.erp.store.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    Store findByMerchantCode(String merchantCode);
}
