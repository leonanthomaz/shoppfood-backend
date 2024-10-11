package com.microshoppe.erp.authentication.dto;

import com.microshoppe.erp.authentication.enums.AccessLevel;
import com.microshoppe.erp.authentication.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {
    private String name;
    private AccessLevel accessLevel;
    private String email;
    private String password;
    private UserRole role;
    private String merchantCode;
}