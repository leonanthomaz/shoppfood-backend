package com.microshoppe.erp.authentication.repository;

import com.microshoppe.erp.authentication.dto.UserDetailsDTO;
import com.microshoppe.erp.authentication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    UserDetails findByEmail(String email);
    Optional<User> findById(Long userId);
    boolean existsByMerchantCode(String merchantCode);
    User findByTelephone(String telephone);
    List<User> findByMerchantCode(String merchantCode);
    User findUserByMerchantCode(String merchantCode);
}
