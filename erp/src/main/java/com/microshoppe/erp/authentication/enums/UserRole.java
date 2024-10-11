package com.microshoppe.erp.authentication.enums;

public enum UserRole {
    SYSTEM_ADMIN("system_admin"),
    ADMIN("admin"),
    USER("user");

    private String role;

    UserRole(String role) {
        this.role = role;
    }
    public String getRole() {
        return role;
    }
}
