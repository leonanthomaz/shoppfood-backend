package com.microshoppe.erp.address.service;

import com.microshoppe.erp.address.dto.AddressDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AddressService {

    private final RestTemplate restTemplate = new RestTemplate();

    public AddressDTO buscarEnderecoPorCep(String cep) {
        String url = "https://brasilapi.com.br/api/cep/v1/" + cep;
        return restTemplate.getForObject(url, AddressDTO.class);
    }
}

