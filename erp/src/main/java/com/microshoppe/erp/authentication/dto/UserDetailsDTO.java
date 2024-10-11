package com.microshoppe.erp.authentication.dto;

import com.microshoppe.erp.address.model.Address;
import com.microshoppe.erp.authentication.enums.AccessLevel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserDetailsDTO {
    private Long id;
    private String name;
    private AccessLevel accessLevel;
    private String merchantCode;
    private String email;
    private String telephone;
    private String resetToken;
    private LocalDateTime resetTokenExpiryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active;
    private boolean anonymous;
    private Address address;
}
