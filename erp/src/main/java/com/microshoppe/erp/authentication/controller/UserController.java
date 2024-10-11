package com.microshoppe.erp.authentication.controller;

import com.microshoppe.erp.address.repository.AddressRepository;
import com.microshoppe.erp.address.service.AddressService;
import com.microshoppe.erp.authentication.dto.UserDetailsDTO;
import com.microshoppe.erp.authentication.dto.UserUpdateDTO;
import com.microshoppe.erp.authentication.model.User;
import com.microshoppe.erp.authentication.service.TokenService;
import com.microshoppe.erp.authentication.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Log4j2
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private final UserService userService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private AddressService addressService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //LISTA TODOS
    @Secured("ROLE_SYSTEM_ADMIN")
    @GetMapping
    public ResponseEntity<List<User>> findAll(){
        return ResponseEntity.status(HttpStatus.OK).body(userService.findAll());
    }

    //PEGA DETALHES DO USUARIO
    @GetMapping("/details")
    public ResponseEntity<UserDetailsDTO> getCurrentUser(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", ""); // Remove o prefixo "Bearer "
        log.info("USER - TOKEN RECUPERADO token: {}", token);

        User user = tokenService.getUserDetailsFromToken(token);
        log.info("USER - USER RECUPERADO user: {}", user);

        if (user != null) {
            UserDetailsDTO userDetailsDTO = UserDetailsDTO.builder()
                    .id(user.getId())
                    .merchantCode(user.getMerchantCode())
                    .name(user.getName())
                    .email(user.getEmail())
                    .telephone(user.getTelephone())
                    .accessLevel(user.getAccessLevel())
                    .build();
            log.info("USER - DADOS RECUPERADOS COM SUCESSO: {}", userDetailsDTO);
            return ResponseEntity.status(HttpStatus.OK).body(userDetailsDTO);
        } else {
            log.error("USER - DADOS NÃO RECUPERADOS -getCurrentUser : {}", user);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/find/merchant/{merchantCode}")
    public ResponseEntity<List<User>> getOrderByMerchantCode(@PathVariable String merchantCode) {
        log.info("USER >>> DADOS RECUPERADOS VIA REQUISIÇÃO - merchantCode: {}", merchantCode);
        try {
            List<User> orders = userService.getByMerchantCode(merchantCode);
            if (orders.isEmpty()) {
                log.info("USER >>> CÓDIGO: {}", merchantCode);
                return ResponseEntity.ok(orders); // Retorna 200 OK com lista vazia
            }
            return ResponseEntity.ok(orders);
        } catch (RuntimeException e) {
            log.error("USER >>> FALHA AO BUSCAR USERS: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Retorna 500 em caso de erro
        }
    }
    
    //ACHA POR TELEFONE
    @GetMapping("/by-phone")
    public ResponseEntity<User> findByPhone(@RequestParam String telephone) {
        log.info("USER - BUSCANDO USUÁRIO PELO TELEFONE: {}", telephone);
        User user = userService.findByTelephone(telephone);
        if (user != null) {
            return ResponseEntity.status(HttpStatus.OK).body(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Atualiza usuário
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO data) {
        log.info("DADOS DE ATUALIZAÇÃO DO USUÁRIO: {}", data);
        User updatedUser = userService.updateUser(id, data);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(updatedUser);
    }

    //DELETA POR ID
    @Secured("ROLE_SYSTEM_ADMIN")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("DELETANDO USUÁRIO COM ID: {}", id);
        User user = userService.findById(id);
        userService.delete(user);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
