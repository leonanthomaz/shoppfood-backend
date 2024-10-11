package com.microshoppe.erp.store.enums;

public enum StoreStatus {
    CREATE("CREATE"),

    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE"),

    OPEN("OPEN"),
    CLOSE("CLOSE");

    private final String status;

    private StoreStatus(String status) {
        this.status = status;
    }

    public String getStatus(){
        return status;
    }

    public static StoreStatus fromCode(String storeStatus) {
        for (StoreStatus status : StoreStatus.values()) {
            if (status.getStatus().equals(storeStatus)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid storeStatus: " + storeStatus);
    }

}
