package com.microshoppe.erp.authentication.dto;

import com.microshoppe.erp.address.model.Address;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserUpdateDTO {
    private Long id;
    private String name;
    private String merchantCode;
    private String email;
    private String password;
    private String telephone;
    private LocalDateTime updatedAt;
    private boolean active;
    private boolean anonymous;
    private Address address;
}
