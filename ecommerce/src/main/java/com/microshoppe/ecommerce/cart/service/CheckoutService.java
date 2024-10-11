package com.microshoppe.ecommerce.cart.service;

import com.microshoppe.ecommerce.cart.dto.CheckoutRequestDTO;
import com.microshoppe.ecommerce.cart.enums.CartStatus;
import com.microshoppe.ecommerce.cart.model.Cart;
import com.microshoppe.ecommerce.cart.repository.CartRepository;
import com.microshoppe.ecommerce.order.dto.OrderDTO;
import com.microshoppe.ecommerce.order.service.OrderService;
import com.microshoppe.erp.address.model.Address;
import com.microshoppe.erp.authentication.dto.UserDetailsDTO;
import com.microshoppe.erp.authentication.enums.UserRole;
import com.microshoppe.erp.authentication.model.User;
import com.microshoppe.erp.authentication.repository.UserRepository;
import com.microshoppe.erp.authentication.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;

    public OrderDTO processCheckout(CheckoutRequestDTO data) {
        log.info("CHECKOUT SERVICE >>> DADOS RECEBIDOS DO CONTROLLER: {}", data);
        User user;
        if (data.getToken() != null && !data.getToken().isEmpty()) {
            if (data.getUser().getId() == null) {
                throw new RuntimeException("CHECKOUT SERVICE >>> ID do usuário não fornecido para atualização.");
            }
            user = updateUserDetails(data.getUser());
            log.info("CHECKOUT SERVICE >>> USUÁRIO ATUALIZADO: {}", user);
        } else {
            // Verifica se já existe um usuário com o mesmo telefone
            user = findOrCreateAnonymousUser(data.getUser());
            log.info("CHECKOUT SERVICE >>> USUÁRIO CRIADO/RECUPERADO: {}", user);
        }

        Cart cart = getCart(data.getCartCode());
        cart.setStatus(CartStatus.CHECKOUT);

        /// Atribui a taxa de entrega ao carrinho
        BigDecimal deliveryFee = data.getDeliveryFee();
        cart.setDeliveryFee(deliveryFee);

        // Calcula o total, incluindo a taxa de entrega
        cart.calculateTotal();

        Cart cartSaved = cartRepository.save(cart);
        log.info("CHECKOUT SERVICE >>> CARRINHO ATUALIZADO: {}", cartSaved);

        return orderService.createOrder(cartSaved, user, deliveryFee);
    }

    private User findOrCreateAnonymousUser(UserDetailsDTO userDetails) {
        // Busca usuário pelo telefone
        User existingUser = userRepository.findByTelephone(userDetails.getTelephone());

        if (existingUser != null) {
            log.info("CHECKOUT SERVICE >>> USUÁRIO ANÔNIMO EXISTENTE ENCONTRADO: {}", existingUser);
            return existingUser;
        }

        // Cria novo usuário anônimo se não existir
        return createAnonymousUser(userDetails);
    }

    private User updateUserDetails(UserDetailsDTO userDetails) {
        log.info("CHECKOUT SERVICE >>> DADOS RECEBIDOS DO USUARIO PRA ATUALIZAÇÃO: {}", userDetails);
        Long userId = userDetails.getId();
        if (userId != null) {
            User existingUser = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + userId));

            // Atualiza informações básicas do usuário
            existingUser.setName(userDetails.getName());
            existingUser.setEmail(userDetails.getEmail());
            existingUser.setTelephone(userDetails.getTelephone());

            // Atualiza ou insere o endereço
            if (userDetails.getAddress() != null) {
                List<Address> userAddresses = existingUser.getAddresses();
                if (userAddresses == null) {
                    userAddresses = new ArrayList<>();
                    existingUser.setAddresses(userAddresses);
                }

                Address addressFromRequest = userDetails.getAddress();
                boolean addressUpdated = false;

                for (Address address : userAddresses) {
                    if (address.getId() != null && address.getId().equals(addressFromRequest.getId())) {
                        // Atualiza o endereço existente
                        address.setCep(addressFromRequest.getCep());
                        address.setStreet(addressFromRequest.getStreet());
                        address.setNumber(addressFromRequest.getNumber());
                        address.setComplement(addressFromRequest.getComplement());
                        address.setNeighborhood(addressFromRequest.getNeighborhood());
                        address.setCity(addressFromRequest.getCity());
                        address.setState(addressFromRequest.getState());
                        addressUpdated = true;
                        break;
                    }
                }

                if (!addressUpdated) {
                    // Adiciona um novo endereço se não encontrado um existente
                    addressFromRequest.setUser(existingUser);
                    userAddresses.add(addressFromRequest);
                }
            }

            existingUser.setUpdatedAt(LocalDateTime.now());
            User updatedUser = userRepository.save(existingUser);
            log.info("CHECKOUT SERVICE >>> USUÁRIO ATUALIZADO: {}", updatedUser);
            return updatedUser;
        }
        throw new RuntimeException("CHECKOUT SERVICE >>> ID do usuário não fornecido");
    }

    private Cart getCart(String cartCode) {
        log.info("CHECKOUT SERVICE >>> CÓDIGO DO CARRINHO RECEBIDO PRA AVALIAÇÃO: {}", cartCode);
        return cartRepository.findByCartCode(cartCode)
                .orElseThrow(() -> new RuntimeException("Carrinho não encontrado para código: " + cartCode));
    }

    private User createAnonymousUser(UserDetailsDTO user) {
        User anonymousUser = User.builder()
                .name(user.getName() != null ? user.getName() : "Visitante")
                .merchantCode(null)
                .telephone(user.getTelephone())
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now())
                .active(true)
                .anonymous(true)
                .build();
        log.info("CHECKOUT SERVICE >>> USUÁRIO ANONIMO CRIADO: {}", anonymousUser);
        return userRepository.save(anonymousUser);
    }
}
