package com.microshoppe.erp.delivery.repository;

import com.microshoppe.erp.delivery.model.DeliveryZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryZoneRepository extends JpaRepository<DeliveryZone, Long> {
}
