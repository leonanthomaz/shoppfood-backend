package com.microshoppe.erp.store.dto;

import com.microshoppe.erp.address.model.Address;
import com.microshoppe.erp.authentication.enums.UserRole;
import com.microshoppe.erp.store.enums.StoreStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StoreUpdateDTO {
    private String name;
    private String merchantCode;
    private Address address;
    private String logoImage;
    private String headerImage;
    private String phoneNumber;
    private Integer deliveryTime;
    private String openingHours;
    private String primaryColor;
    private BigDecimal minimumValue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean open;
    private boolean active;
    private boolean isNewStore;
    private StoreStatus status;
    private UserRole userRole;
}
