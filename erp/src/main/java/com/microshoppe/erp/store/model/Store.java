package com.microshoppe.erp.store.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.microshoppe.erp.address.model.Address;
import com.microshoppe.erp.authentication.model.User;
import com.microshoppe.erp.store.enums.StoreStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_store")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String merchantCode;

    private String logoImage;
    private String headerImage;
    private String primaryColor;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonBackReference(value = "user-store")
    @ToString.Exclude
    private User user;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "id", nullable = true)
    @JsonManagedReference(value = "store-address")
    @ToString.Exclude
    private Address address;

    private String phoneNumber;
    private boolean open;
    private Integer deliveryTime;
    private String openingHours;
    private BigDecimal minimumValue;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean isNewStore;
    private boolean active;
    private StoreStatus status;

}
