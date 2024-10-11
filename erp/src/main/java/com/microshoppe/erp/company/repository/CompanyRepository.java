package com.microshoppe.erp.company.repository;

import com.microshoppe.erp.company.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    // Adicione métodos customizados aqui, se necessário
}
