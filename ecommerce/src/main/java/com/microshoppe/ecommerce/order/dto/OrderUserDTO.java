package com.microshoppe.ecommerce.order.dto;

import com.microshoppe.erp.authentication.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderUserDTO {

    private String name;
    private String merchantCode;
    private String telephone;
    private UserRole role;
    private LocalDateTime createdAt;
    private boolean active;
    private boolean anonymous;
}
