package com.microshoppe.erp.delivery.repository;

import com.microshoppe.erp.delivery.model.Delivery;
import com.microshoppe.erp.delivery.model.DeliveryZone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Delivery findByMerchantCode(String merchantCode);
}
