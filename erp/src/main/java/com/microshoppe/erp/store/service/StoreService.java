package com.microshoppe.erp.store.service;

import com.microshoppe.erp.address.model.Address;
import com.microshoppe.erp.address.repository.AddressRepository;
import com.microshoppe.erp.authentication.model.User;
import com.microshoppe.erp.authentication.repository.UserRepository;
import com.microshoppe.erp.authentication.service.AuthService;
import com.microshoppe.erp.store.dto.StoreDTO;
import com.microshoppe.erp.store.dto.StoreUpdateDTO;
import com.microshoppe.erp.store.enums.StoreStatus;
import com.microshoppe.erp.store.model.Store;
import com.microshoppe.erp.store.repository.StoreRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Log4j2
@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final AuthService authService;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public StoreService(StoreRepository storeRepository, @Lazy AuthService authService, AddressRepository addressRepository, UserRepository userRepository) {
        this.storeRepository = storeRepository;
        this.authService = authService;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    public Store createNewStore(StoreDTO data) {
        log.info("STORE SERVICE >>> CRIAÇÃO DE LOJA >>> DADOS RECEBIDOS: {}", data);

        User newUser = authService.registerStoreUser(data.getUser());
        log.info("STORE SERVICE >>> NOVO USUÁRIO CRIADO >>> {}", newUser);

        Store store = Store.builder()
                .name(data.getName())
                .merchantCode(newUser.getMerchantCode())
                .user(newUser)
                .active(true)
                .isNewStore(true)
                .status(StoreStatus.CREATE)
                .createdAt(LocalDateTime.now())
                .build();
        log.info("STORE SERVICE >>> NOVA LOJA CRIADA >>> {}", store);

        return storeRepository.save(store);
    }

    public Store saveStore(Store store) {
        log.info("STORE SERVICE >>> CRIAÇÃO DE LOJA >>> DADOS RECEBIDOS: {}", store);
        return storeRepository.save(store);
    }

    public Store updateStore(StoreUpdateDTO data) {
        log.info("STORE SERVICE >>> DADOS RECEBIDOS >>> {}", data);
        Store existingStore = storeRepository.findByMerchantCode(data.getMerchantCode());
        if (existingStore == null) {
            throw new RuntimeException("Loja não encontrada com o merchantCode: " + data.getMerchantCode());
        }
        log.info("STORE SERVICE >>> LOJA EXISTENTE >>> {}", existingStore);

        // Busque o usuário pelo merchantCode
        User user = userRepository.findUserByMerchantCode(existingStore.getMerchantCode());
        if (user == null) {
            throw new RuntimeException("Usuário não encontrado para o merchantCode: " + existingStore.getMerchantCode());
        }
        log.info("STORE SERVICE >>> USUÁRIO EXISTENTE >>> {}", user);


        // Atualiza os dados da loja
        existingStore.setName(data.getName());
        existingStore.setPhoneNumber(data.getPhoneNumber());
        existingStore.setDeliveryTime(data.getDeliveryTime());
        existingStore.setLogoImage(data.getLogoImage());
        existingStore.setHeaderImage(data.getHeaderImage());
        existingStore.setOpeningHours(data.getOpeningHours());
        existingStore.setMinimumValue(data.getMinimumValue());
        existingStore.setUpdatedAt(LocalDateTime.now());
        existingStore.setPrimaryColor(data.getPrimaryColor());

        log.info("STORE SERVICE >>> LOJA SENDO ATUALIZADA >>> {}", existingStore);

        // Verifica se o endereço é nulo ou se já está associado à loja
        Address newAddress = data.getAddress();
        if (newAddress != null) {
            if (newAddress.getId() == null) {
                newAddress.setStore(existingStore);
                newAddress.setUser(user); // Associe o usuário ao novo endereço
                existingStore.setAddress(addressRepository.save(newAddress));

                log.info("STORE SERVICE >>> NOVO ENDEREÇO >>> {}", existingStore);

            } else {
                // Caso o endereço já exista, apenas atualize as informações
                Address existingAddress = addressRepository.findById(newAddress.getId())
                        .orElseThrow(() -> new RuntimeException("Endereço não encontrado com o ID: " + newAddress.getId()));

                log.info("STORE SERVICE >>> ENDEREÇO ENCONTRADO >>> {}", existingStore);


                // Atualiza o endereço existente
                existingAddress.setCep(newAddress.getCep());
                existingAddress.setCity(newAddress.getCity());
                existingAddress.setNeighborhood(newAddress.getNeighborhood());
                existingAddress.setStreet(newAddress.getStreet());
                existingAddress.setNumber(newAddress.getNumber());
                existingAddress.setComplement(newAddress.getComplement());
                existingAddress.setState(newAddress.getState());
                existingAddress.setMerchantCode(existingStore.getMerchantCode());
                existingAddress.setUser(user);

                log.info("STORE SERVICE >>> SETANDO ENDEREÇO EXISTENTE >>> {}", existingStore);


                // Salva as alterações no endereço
                addressRepository.save(existingAddress);
                existingStore.setAddress(existingAddress);
            }
        }

        // Salva a loja atualizada
        return storeRepository.save(existingStore);
    }

    public Store findByMerchantCode(String merchantCode){
        Store store = storeRepository.findByMerchantCode(merchantCode);
        if (store == null) {
            throw new RuntimeException("Loja não encontrada.");
        }
        return storeRepository.findByMerchantCode(merchantCode);
    }

    public void updateStoreLogo(String merchantCode, String logoUrl) {
        Store store = storeRepository.findByMerchantCode(merchantCode);
        if (store == null) {
            throw new RuntimeException("Loja não encontrada.");
        }
        store.setLogoImage(logoUrl);
        store.setUpdatedAt(LocalDateTime.now());
        storeRepository.save(store);
    }

    public void updateStoreHeader(String merchantCode, String headerUrl) {
        Store store = storeRepository.findByMerchantCode(merchantCode);
        if (store == null) {
            throw new RuntimeException("Loja não encontrada.");
        }
        store.setHeaderImage(headerUrl);
        store.setUpdatedAt(LocalDateTime.now());
        storeRepository.save(store);
    }

    public void deleteStore(String merchantCode) {
        Store existingStore = storeRepository.findByMerchantCode(merchantCode);
        storeRepository.delete(existingStore);
    }

    public Store activeStore(String merchantCode, boolean condition) {
        Store existingStore = findByMerchantCode(merchantCode);
        existingStore.setActive(condition);
        return existingStore;
    }

}
