package com.microshoppe.erp.store.dto;

import com.microshoppe.erp.authentication.enums.AccessLevel;
import com.microshoppe.erp.authentication.enums.UserRole;
import lombok.Data;

@Data
public class StoreRegisterUserDTO {
    private String name;
    private String email;
    private UserRole role;
    private AccessLevel accessLevel;
    private String password;
    private String merchantCode;
}
