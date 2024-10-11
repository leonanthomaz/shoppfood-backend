package com.microshoppe.erp.authentication.controller;

import com.microshoppe.erp.authentication.dto.PasswordResetRequestDTO;
import com.microshoppe.erp.authentication.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/recover-password")
    public ResponseEntity<String> requestPasswordReset(@RequestBody PasswordResetRequestDTO data) {
        log.info("PASSWORD RECOVER - DADOS RECUPERADOS VIA REQUISIÇÃO: {}", data);
        try {
            passwordResetService.processingPasswordSetupEmail(data.getEmail());
            return ResponseEntity.status(HttpStatus.OK).body(data.getEmail());
        } catch (RuntimeException e) {
            log.error("PASSWORD RECOVER- ERRO AO RECUPERAR EMAIL: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> payload) {
        log.info("PASSWORD RESET - DADOS RECUPERADOS VIA REQUISIÇÃO: {}", payload);
        String token = payload.get("token");
        String newPassword = payload.get("newPassword");

        log.info("PARAMETROS RECEBIDOS NO RESET-PASSWORD - TOKEN: {} - SENHA NOVA: {}", token, newPassword);

        boolean success = passwordResetService.resetPassword(token, newPassword);
        if (success) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("SENHA ALTERADA COM SUCESSO!");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token.");
        }
    }

    @PostMapping("/reset-password/new-store")
    public ResponseEntity<String> resetPasswordNewStore(@RequestBody Map<String, String> payload) {
        log.info("PASSWORD RESET - DADOS RECUPERADOS VIA REQUISIÇÃO: {}", payload);
        String token = payload.get("token");
        String newPassword = payload.get("newPassword");

        log.info("PARAMETROS RECEBIDOS NO RESET-PASSWORD - TOKEN: {} - SENHA NOVA: {}", token, newPassword);

        boolean success = passwordResetService.resetPasswordNewStore(token, newPassword);
        if (success) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("SENHA ALTERADA COM SUCESSO!");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token.");
        }
    }
}
