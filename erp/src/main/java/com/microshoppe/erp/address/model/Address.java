package com.microshoppe.erp.address.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.microshoppe.erp.authentication.model.User;
import com.microshoppe.erp.store.model.Store;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_address")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String merchantCode;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference(value = "user-address")
    @ToString.Exclude
    private User user;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = true)
    @JsonBackReference(value = "store-address")
    @ToString.Exclude
    private Store store;

    private String cep;
    private String state;
    private String city;
    private String neighborhood;
    private String street;
    private String number;
    private String complement;
}


