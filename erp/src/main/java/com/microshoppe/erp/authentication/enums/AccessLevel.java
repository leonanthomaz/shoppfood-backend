package com.microshoppe.erp.authentication.enums;

public enum AccessLevel {
    SYSTEM_ADMIN("SYSTEM_ADMIN"),
    ADMIN("ADMIN"),
    USER("USER");

    private final String acessLevel;

    AccessLevel(String acessLevel) {
        this.acessLevel = acessLevel;
    }

    public String getAcessLevel() {
        return acessLevel;
    }

    public static AccessLevel fromCode(String acessLevel) {
        for (AccessLevel status : AccessLevel.values()) {
            if (status.getAcessLevel().equals(acessLevel)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid orderCode: " + acessLevel);
    }
}
