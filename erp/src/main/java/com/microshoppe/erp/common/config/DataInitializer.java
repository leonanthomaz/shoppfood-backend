package com.microshoppe.erp.common.config;

import com.microshoppe.erp.authentication.enums.AccessLevel;
import com.microshoppe.erp.authentication.enums.UserRole;
import com.microshoppe.erp.authentication.model.User;
import com.microshoppe.erp.authentication.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    private String generateRandomMerchantCode() {
        String uuid = UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "");
        return uuid.substring(0, Math.min(uuid.length(), 32)).toUpperCase();
    }

    @Override
    public void run(String... args) throws Exception {
        // Configura as credenciais do administrador
        String adminEmail = "leonan.thomaz@gmail.com";
        String adminPassword = "leonan2knet";
        String adminName = "Leonan Thomaz de Oliveira";
        String adminTelephone = "21998090928";

        if (userService.findByEmail(adminEmail) == null) {
            String encryptedPassword = new BCryptPasswordEncoder().encode(adminPassword);

            User adminUser = User.builder()
                    .merchantCode(generateRandomMerchantCode())
                    .name(adminName)
                    .email(adminEmail)
                    .password(encryptedPassword)
                    .telephone(adminTelephone)
                    .accessLevel(AccessLevel.SYSTEM_ADMIN)
                    .role(UserRole.SYSTEM_ADMIN)
                    .build();

            userService.save(adminUser);
            System.out.println("Usuário administrador criado com sucesso: " + adminEmail);
        } else {
            System.out.println("Usuário administrador já existe.");
        }
    }
}