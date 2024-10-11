package com.microshoppe.erp.authentication.controller;

import com.microshoppe.erp.authentication.dto.LoginDTO;
import com.microshoppe.erp.authentication.dto.RegisterDTO;
import com.microshoppe.erp.authentication.dto.TokenDTO;
import com.microshoppe.erp.authentication.exception.AuthException;
import com.microshoppe.erp.authentication.model.User;
import com.microshoppe.erp.authentication.service.AuthService;
import com.microshoppe.erp.store.dto.StoreRegisterUserDTO;
import jakarta.validation.Valid;
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
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@RequestBody @Valid LoginDTO data) {
        log.info("AUTH (LOGIN) - DADOS RECUPERADOS VIA REQUISIÇÃO - login: {}", data);
        try {
            TokenDTO tokenDTO = authService.login(data);
            return ResponseEntity.ok(tokenDTO);
        } catch (AuthException e) {
            log.error("AUTH (LOGIN) - ERRO AO PROCESSAR LOGIN: {}", e);
            throw new AuthException("AUTH - FALHA AO PROCESSAR LOGIN: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<TokenDTO> register(@RequestBody @Valid RegisterDTO data) {
        log.info("AUTH (REGISTRO) - DADOS RECUPERADOS VIA REQUISIÇÃO - register: {}", data);
        try {
            TokenDTO tokenDTO = authService.register(data);
            return ResponseEntity.ok(tokenDTO);
        } catch (AuthException e) {
            log.error("AUTH (REGISTRO) - ERRO AO PROCESSAR REGISTRO DO USUÁRIO: {}", e.getMessage());
            throw new AuthException("AUTH (REGISTRO) - FALHA AO REALIZAR REGISTRO: " + e.getMessage());
        }
    }

    @PostMapping("/register/store")
    public ResponseEntity<User> registerStore(@RequestBody @Valid StoreRegisterUserDTO data) {
        log.info("AUTH (REGISTRO DE COMERCIANTE) - DADOS RECUPERADOS VIA REQUISIÇÃO - dados: {}", data);
        try {
            User user = authService.registerStoreUser(data);
            return ResponseEntity.ok(user);
        } catch (AuthException e) {
            log.error("AUTH (REGISTRO DE COMERCIANTE) - ERRO AO PROCESSAR REGISTRO DO USUÁRIO: {}", e.getMessage());
            throw new AuthException("AUTH (REGISTRO) - FALHA AO REALIZAR REGISTRO: " + e.getMessage());
        }
    }

    @PostMapping("/login/oauth2/google")
    public ResponseEntity<TokenDTO> googleAuthLogin(@RequestBody Map<String, String> payload) {
        log.info("AUTH (GOOGLE) - googleAuthLogin - DADOS RECUPERADOS VIA REQUISIÇÃO - payload: {}", payload);
        try {
            TokenDTO tokenDTO = authService.googleAuthLogin(payload);
            return ResponseEntity.status(HttpStatus.CREATED).body(tokenDTO);
        } catch (AuthException e) {
            log.error("AUTH (GOOGLE) - ERRO AO AUTENTICAR/REGISTRAR LOGIN COM GOOGLE: {}", e.getMessage());
            throw new AuthException("AUTH (GOOGLE) - ERRO AO AUTENTICAR/REGISTRAR LOGIN COM GOOGLE: " + e.getMessage());
        }
    }
}
