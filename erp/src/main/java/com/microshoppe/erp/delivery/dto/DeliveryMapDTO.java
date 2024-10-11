package com.microshoppe.erp.delivery.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DeliveryMapDTO {
    @NotNull
    private List<Neighborhood> neighborhoods;

    @NotNull
    private String cep;

    @NotNull
    private Location centralPoint;

    @NotNull
    private BigDecimal defaultDeliveryFee;

    @NotNull
    private double radius;

    @Data
    public static class Neighborhood {
        private String name;
        private double price;
        private double lat;
        private double lng;
    }

    @Data
    public static class Location {
        private double lat;
        private double lng;
    }
}
