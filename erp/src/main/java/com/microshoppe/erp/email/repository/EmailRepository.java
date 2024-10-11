package com.microshoppe.erp.email.repository;

import com.microshoppe.erp.email.model.Email;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRepository extends JpaRepository<Email, Long> {
}
