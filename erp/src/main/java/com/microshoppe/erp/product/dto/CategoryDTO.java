package com.microshoppe.erp.product.dto;

import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private String merchantCode;
    private String description;
}
