package com.microshoppe.erp.authentication.service;

import com.microshoppe.erp.address.dto.AddressDTO;
import com.microshoppe.erp.address.model.Address;
import com.microshoppe.erp.address.repository.AddressRepository;
import com.microshoppe.erp.address.service.AddressService;
import com.microshoppe.erp.authentication.dto.UserDetailsDTO;
import com.microshoppe.erp.authentication.dto.UserUpdateDTO;
import com.microshoppe.erp.authentication.model.User;
import com.microshoppe.erp.authentication.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Log4j2
@Service
public class UserService implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AddressService addressService;
    @Autowired
    private AddressRepository addressRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = (User) userRepository.findByEmail(identifier);
        return user;
    }

    //LISTA TODOS
    public List<User> findAll() {
        return userRepository.findAll();
    }

    //ACHA POR EMAIL
    public UserDetails findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    //ACHA POR ID
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("USUÁRIO NAO ENCONTRADO!"));
    }

    //ACHA POR TELEFONE
    public User findByTelephone(String telephone) {
        return userRepository.findByTelephone(telephone);
    }

    // Atualiza usuário
    public User updateUser(Long id, UserUpdateDTO data) {
        User user = findById(id);
        user.setName(data.getName());
        user.setEmail(data.getEmail());
        user.setTelephone(data.getTelephone());

        // Atualiza ou cria o endereço
        Address address = data.getAddress().getId() != null
                ? addressRepository.findById(data.getAddress().getId())
                .orElseThrow(() -> new RuntimeException("Endereço não encontrado"))
                : new Address();

        address.setUser(user);
        address.setCep(data.getAddress().getCep());
        AddressDTO addressFind = addressService.buscarEnderecoPorCep(data.getAddress().getCep());
        address.setCity(addressFind.getCity());
        address.setState(addressFind.getState());
        address.setNeighborhood(addressFind.getNeighborhood());
        address.setStreet(addressFind.getStreet());
        address.setNumber(data.getAddress().getNumber());
        address.setComplement(data.getAddress().getComplement());

        addressRepository.save(address);
        user.setAddresses((List<Address>) address);

        log.info("USUARIO: {}", user);
        log.info("ENDEREÇO: {}", address);

        return userRepository.save(user);
    }

    //SALVA USUARIO
    public User save(User user) {
        return userRepository.save(user);
    }

    //DELETA USUARIO
    public void delete(User user) {
        userRepository.delete(user);
    }

    public List<User> getByMerchantCode(String merchantCode) {
        return userRepository.findByMerchantCode(merchantCode);

    }
}
