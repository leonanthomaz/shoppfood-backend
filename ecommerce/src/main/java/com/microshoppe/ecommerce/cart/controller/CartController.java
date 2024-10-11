package com.microshoppe.ecommerce.cart.controller;

import com.microshoppe.ecommerce.cart.dto.*;
import com.microshoppe.ecommerce.cart.model.Cart;
import com.microshoppe.ecommerce.cart.model.CartItem;
import com.microshoppe.ecommerce.cart.service.CartService;
import com.microshoppe.erp.common.dto.MerchantCodeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/create")
    public ResponseEntity<CartDTO> createCart(@RequestBody MerchantCodeDTO data) {
        log.info("ROTA CART >>> DADOS VINDOS DA REQUISIÇÃO: {}", data.getMerchantCode());
        try {
            Cart cart = cartService.createCart(data.getMerchantCode());
            log.info("ROTA CART >>> CARRINHO CRIADO! {}", cart);
            return ResponseEntity.status(HttpStatus.CREATED).body(new CartDTO(cart));
        } catch (RuntimeException e) {
            log.error("ROTA CART >>> Erro ao criar carrinho: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CartDTO(null));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<CartDTO>> findAll() {
        log.info("ROTA CART >>> LISTA DE VALIDAÇÕES: Iniciando busca");
        try {
            List<Cart> carts = cartService.findAll();
            log.info("ROTA CART >>> LISTA: {}", carts);
            List<CartDTO> cartDTOS = carts.stream().map(CartDTO::new).toList();
            return ResponseEntity.status(HttpStatus.OK).body(cartDTOS);
        } catch (RuntimeException e) {
            log.error("ROTA CART >>> Erro ao lista de Validação: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/find/{cartCode}")
    public ResponseEntity<CartDTO> getCart(@PathVariable("cartCode") String cartCode) {
        log.info("ROTA CART >>> BUSCANDO CARRINHO COM CÓDIGO: {}", cartCode);
        try {
            Cart cart = cartService.getCartByCartCode(cartCode);
            log.info("ROTA CART >>> VALIDAÇÃO ENCONTRADO: {}", cart);
            return ResponseEntity.status(HttpStatus.OK).body(new CartDTO(cart));
        } catch (RuntimeException e) {
            log.error("ROTA CART >>>  Erro ao buscar o carrinho: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    @GetMapping("/find/{cartCode}/product/{productId}")
    public ResponseEntity<CartItemDTO> getProductInCart(
            @PathVariable("cartCode") String cartCode,
            @PathVariable("productId") Long productId) {
        log.info("ROTA VALIDAÇÃO -: BUSCANDO PRODUTO COM ID: {} NO CARRINHO COM CÓDIGO: {}", productId, cartCode);
        try {
            CartItem cartItem = cartService.getProductInCart(cartCode, productId);
            if (cartItem != null) {
                log.info("ROTA VALIDAÇÃO - PRODUTO ENCONTRADO: {}", cartItem);
                return ResponseEntity.status(HttpStatus.OK).body(new CartItemDTO(cartItem));
            } else {
                log.info("ROTA VALIDAÇÃO - PRODUTO NÃO ENCONTRADO NO CARRINHO");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (RuntimeException e) {
            log.error("ROTA VALIDAÇÃO - Erro ao buscar o produto no carrinho: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<CartDTO> addItem(
            @RequestParam String merchantCode,
            @RequestParam(name = "cartCode", required = false) String cartCode,
            @RequestParam String codeProduct
    ) {
        log.info("ROTA CART >>>  ADICIONANDO ITEM: merchantCode={}, cartCode={}, getCodeProduct={}", merchantCode, cartCode, codeProduct);

        if (codeProduct.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        try {
            Cart cart = cartService.addItem(merchantCode, cartCode, codeProduct);
            log.info("ROTA CART >>> ITEM ADICIONADO: {}", cart);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new CartDTO(cart));
        } catch (RuntimeException e) {
            log.error("ROTA CART >>> Erro ao adicionar item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/insert")
    public ResponseEntity<?> insertCartItemInCart(
            @RequestParam String merchantCode,
            @RequestParam String cartCode,
            @RequestParam String codeProduct
    ) {
        log.info("ROTA CART >>>  ADICIONANDO XERECA: merchantCode={}, cartCode={}, getCodeProduct={}", merchantCode, cartCode, codeProduct);
        if (codeProduct.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        try {
            cartService.insertCartItemInCart(merchantCode, cartCode, codeProduct);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("XERECA >>> PRODUTO ADICIONADO AO CARRINHO!!!");
        } catch (RuntimeException e) {
            log.error("ROTA CART >>> Erro ao adicionar item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/add-observation")
    public ResponseEntity<?> insertObservation(
            @RequestParam String cartCode,
            @RequestParam String codeProduct,
            @RequestBody ObservationRequestDTO data
    ) {
        log.info("ROTA CART >>> ADICIONANDO OBSERVAÇÃO: cartCode={}, codeProduct={}, observation={}", cartCode, codeProduct, data);

        // Valida se o código do produto foi fornecido
        if (codeProduct == null || codeProduct.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("O código do produto não pode ser vazio.");
        }

        try {
            // Insere a observação no carrinho
            cartService.insertObservationInCart(cartCode, codeProduct, data.getObservation());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Observação inserida com sucesso!");
        } catch (RuntimeException e) {
            log.error("ROTA CART >>> Erro ao adicionar observação: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao adicionar observação: " + e.getMessage());
        }
    }


    @PutMapping("/increment")
    public ResponseEntity<CartDTO> incrementItem(
            @RequestBody ProductChangeRequestDTO data
    ) {
        log.info("ROTA CART >>> INCREMENTANDO ITEM: cartCode={},  getProductId={}, getCodeProduct={}", data.getCartCode(), data.getProductId(), data.getCodeProduct());
        try {
            Cart cart = cartService.incrementItem(data.getCartCode(), data.getProductId(), data.getCodeProduct());
            log.info("ROTA CART >>> ITEM INCREMENTADO: {}", cart);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new CartDTO(cart));
        } catch (RuntimeException e) {
            log.error("ROTA CART >>> Erro ao incrementar item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PutMapping("/decrement")
    public ResponseEntity<CartDTO> decrementItem(
            @RequestBody ProductChangeRequestDTO data
    ) {
        log.info("ROTA CART >>> DECREMENTANDO ITEM: cartCode={},  getProductId={}, getCodeProduct={}", data.getCartCode(), data.getProductId(), data.getCodeProduct());
        try {
            Cart cart = cartService.decrementItem(data.getCartCode(), data.getProductId(), data.getCodeProduct());
            log.info("ROTA CART >>> ITEM DECREMENTADO: {}", cart);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new CartDTO(cart));
        } catch (RuntimeException e) {
            log.error("ROTA CART >>> Erro ao decrementar item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    //REMOVE UM PRODUTO DO CARRINHO
    @DeleteMapping("/remove")
    public ResponseEntity<CartDTO> removeItem(
            @RequestParam(name = "cartCode") String cartCode,
            @RequestParam(name = "codeProduct") String codeProduct
    ) {
        log.info("ROTA CART >>> REMOVENDO ITEM: cartCode={}, codeProduct={}", cartCode, codeProduct);
        try {
            Cart cart = cartService.removeItem(cartCode, codeProduct);
            log.info("ROTA CART >>> ITEM REMOVIDO: {}", cart);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new CartDTO(cart));
        } catch (RuntimeException e) {
            log.error("ROTA CART >>> Erro ao remover item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    //LIMPA O CARRINHO
    @DeleteMapping("/clear")
    public ResponseEntity<CartDTO> clearCart(@RequestParam String cartCode) {
        log.info("ROTA CART >>> LIMPANDO CARRINHO: cartCode={}", cartCode);
        try {
            Cart cart = cartService.clearCart(cartCode);
            log.info("ROTA CART >>> CARRINHO LIMPO: {}", cart);
            return ResponseEntity.status(HttpStatus.OK).body(new CartDTO(cart));
        } catch (RuntimeException e) {
            log.error("ROTA CART >>> Erro ao limpar carrinho: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    //DELETA O CARRINHO
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteCart(@RequestParam String cartCode) {
        log.info("ROTA CART >>> DELETANDO CARRINHO: cartCode={}", cartCode);
        try {
            cartService.deleteCart(cartCode);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("CARRINHO DELETADO COM SUCESSO!");
        } catch (RuntimeException e) {
            log.error("ROTA CART >>> Erro ao deletar carrinho: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("ROTA CART >>> Erro ao deletar o carrinho.");
        }
    }

    @PutMapping("/increment-option")
    public ResponseEntity<CartDTO> incrementOptionQuantity(
            @RequestBody OptionChangeRequestDTO data
    ) {
        log.info("ROTA CART >>> INCREMENTANDO QUANTIDADE DA OPÇÃO: cartCode={}, getCodeProduct={}, optionId={}", data.getCartCode(), data.getCodeProduct(), data.getCodeOption());
        try {
            Cart cart = cartService.incrementOptionQuantity(data.getCartCode(), data.getCodeProduct(), data.getCodeOption());
            log.info("ROTA CART >>> QUANTIDADE DA OPÇÃO INCREMENTADA: {}", cart);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new CartDTO(cart));
        } catch (RuntimeException e) {
            log.error("ROTA CART >>> Erro ao incrementar quantidade da opção: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/decrement-option")
    public ResponseEntity<CartDTO> decrementOptionQuantity(
            @RequestBody OptionChangeRequestDTO data
    ) {
        log.info("ROTA CART >>> DECREMENTANDO QUANTIDADE DA OPÇÃO: cartCode={}, getCodeProduct={}, optionId={}", data.getCartCode(), data.getCodeProduct(), data.getCodeOption());
        try {
            Cart cart = cartService.decrementOptionQuantity(data.getCartCode(), data.getCodeProduct(), data.getCodeOption());
            log.info("ROTA CART >>> QUANTIDADE DA OPÇÃO DECREMENTADA: {}", cart);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new CartDTO(cart));
        } catch (RuntimeException e) {
            log.error("ROTA CART >>> Erro ao decrementar quantidade da opção: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }




}
