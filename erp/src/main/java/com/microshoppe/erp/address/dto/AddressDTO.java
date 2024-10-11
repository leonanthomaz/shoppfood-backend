package com.microshoppe.erp.address.dto;

import lombok.Data;

@Data
public class AddressDTO {
    private String cep;
    private Long userId;
    private String merchantCode;
    private String state;
    private String city;
    private String neighborhood;
    private String street;
    private String number;
    private String complement;
}


