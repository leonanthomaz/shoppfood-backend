package com.microshoppe.erp.authentication.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class PasswordResetRequestDTO {
    @Email
    private String email;
}
