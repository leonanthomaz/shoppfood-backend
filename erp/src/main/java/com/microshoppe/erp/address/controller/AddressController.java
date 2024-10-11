package com.microshoppe.erp.address.controller;


import com.microshoppe.erp.address.dto.AddressDTO;
import com.microshoppe.erp.address.model.Address;
import com.microshoppe.erp.address.repository.AddressRepository;
import com.microshoppe.erp.address.service.AddressService;
import com.microshoppe.erp.authentication.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Log4j2
@RestController
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{cep}")
    public ResponseEntity<AddressDTO> getAddressByCep(@PathVariable String cep) {
        log.info("CEP RECUPERADO VIA REQUISIÇÃO: {}", cep);
        try{
            AddressDTO address = addressService.buscarEnderecoPorCep(cep);
            log.info("CEP - ENDEREÇO ENCONTRADO: {}", address);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(address);
        } catch (RuntimeException e) {
            log.error("CEP - ERRO AO PROCESSAR CEP: {}", e.getMessage());
            throw new RuntimeException("CEP -  ERRO AO PROCESSAR CEP: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Address> updateAddress(@PathVariable Long id, @RequestBody AddressDTO addressDTO) {
        log.info("ENDEREÇO RECEBIDO PARA ATUALIZAÇÃO: {}", addressDTO);
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ENDEREÇO NÃO ENCONTRADO " + id));

        address.setStreet(addressDTO.getStreet());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setCep(addressDTO.getCep());
        address.setNeighborhood(addressDTO.getNeighborhood());
        address.setNumber(addressDTO.getNumber());
        address.setComplement(addressDTO.getComplement());

        Address updatedAddress = addressRepository.save(address);
        log.info("ENDEREÇO ATUALIZADO E SALVO NO BANCO: {}", updatedAddress);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(updatedAddress);
    }

    @GetMapping("/find/{userId}")
    public ResponseEntity<Optional<Address>> getAddressById(@PathVariable Long userId) {
        log.info("CEP ID RECUPERADO VIA REQUISIÇÃO: {}", userId);
        try{
            Optional<Address> address = addressRepository.findByUserId(userId);
            log.info("ENDEREÇO ENCONTRADO: {}", address);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(address);
        } catch (RuntimeException e) {
            log.error("ERRO AO PROCESSAR ENDEREÇO: {}", e.getMessage());
            throw new RuntimeException("CEP -  ERRO AO PROCESSAR CEP: " + e.getMessage());
        }
    }

}

