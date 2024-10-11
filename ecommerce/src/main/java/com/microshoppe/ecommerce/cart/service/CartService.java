package com.microshoppe.ecommerce.cart.service;

import com.microshoppe.ecommerce.cart.enums.CartItemStatus;
import com.microshoppe.ecommerce.cart.enums.CartStatus;
import com.microshoppe.ecommerce.cart.model.Cart;
import com.microshoppe.ecommerce.cart.model.CartItem;
import com.microshoppe.ecommerce.cart.model.CartItemOption;
import com.microshoppe.ecommerce.cart.repository.CartItemOptionRepository;
import com.microshoppe.ecommerce.cart.repository.CartItemRepository;
import com.microshoppe.ecommerce.cart.repository.CartRepository;
import com.microshoppe.erp.product.model.Product;
import com.microshoppe.erp.product.model.ProductItem;
import com.microshoppe.erp.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemOptionRepository cartItemOptionRepository;
    private final ProductRepository productRepository;

    private String generateRandomCartCode() {
        String uuid = UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "");
        return uuid.substring(0, Math.min(uuid.length(), 8)).toUpperCase();
    }

    @Cacheable(value = "cart", key = "#result.cartCode")
    public Cart getCartByCartCode(String cartCode) {
        log.info("ROTA CART SERVICE - ACHANDO CARRINHO PELO CÓDIGO: {}", cartCode);
        return cartRepository.findByCartCode(cartCode)
                .orElseThrow(() -> new RuntimeException("CARRINHO NÃO ENCONTRADO"));
    }

    public CartItem getProductInCart(String cartCode, Long productId) {
        log.info("ROTA CART SERVICE - ACHANDO PRODUTO EM UM CARRINHO: {} - {}", cartCode, productId);
        Cart cart = getCartByCartCode(cartCode); // Presumindo que você já tem esse método
        if (cart == null) {
            throw new RuntimeException("Carrinho não encontrado");
        }
        return cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    public List<Cart> findAll() {
        log.info("ROTA CART SERVICE - LISTANDO TODOS OS CARRINHOS");
        return cartRepository.findAll();
    }

    public Cart createCart(String merchantCode) {
        log.info("ROTA CART SERVICE - CRIANDO CARRINHO COM CÓDIGO DE CLIENTE: {}", merchantCode);
        String generateCartCode = generateRandomCartCode();

        Cart cart = Cart.builder()
                .cartCode(generateCartCode)
                .merchantCode(merchantCode)
                .status(CartStatus.CREATED)
                .items(new ArrayList<>())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .total(BigDecimal.ZERO)
                .build();
        log.info("ROTA CART SERVICE - CARRINHO PARA SER SALVO: {}", merchantCode);

        return cartRepository.save(cart);
    }

    public Cart addItem(String merchantCode, String cartCode, String codeProduct) {
        log.info("ROTA CART SERVICE - ADD - RECEBENDO DADOS DO CONTROLLER: {} - {} - {}", merchantCode, cartCode, codeProduct);

        Cart cart = cartCode != null && !cartCode.isEmpty()
                ? getCartByCartCode(cartCode)
                : cartRepository.findByMerchantCode(merchantCode)
                .orElseGet(() -> createCart(merchantCode));
        log.info("ROTA CART SERVICE - ADD - ACHANDO CARRINHO: {}", cart);

        // Acha o produto no sistema
        Product product = productRepository.findByCodeProduct(codeProduct)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado no sistema: " + codeProduct));
        log.info("ROTA CART SERVICE - ADD - ACHANDO PRODUTO PELO CÓDIGO: {}", product);

        // Busca o item do carrinho no banco de dados
        CartItem cartItem = cartItemRepository.findByCartIdAndCodeProduct(cart.getId(), product.getCodeProduct());

        if (cartItem == null) {
            // Adiciona o item com quantidade 1
            CartItem newCartItem = CartItem.builder()
                    .codeProduct(product.getCodeProduct())
                    .cart(cart)
                    .product(product)
                    .quantity(1)
                    .totalPrice(product.getPrice())
                    .options(new ArrayList<>())
                    .additionalOptions(new ArrayList<>())
                    .build();
            cart.getItems().add(newCartItem);
            cartItemRepository.save(newCartItem);
            log.info("ROTA CART SERVICE - ADD - Novo item adicionado ao carrinho: {}", newCartItem);
        } else {
            updateCartItemTotal(cartItem);
//            cartItem.setQuantity(cartItem.getQuantity() + 1);
            log.info("ROTA CART SERVICE - ADD - Item existente atualizado no carrinho: {}", cartItem);
        }

        // Atualiza o total do carrinho
        cart.calculateTotal();
        cart.setUpdatedAt(Instant.now());
        cart.setStatus(CartStatus.ACTIVE);
        validateCartItem(cart, product.getCodeProduct(), product.getGetMinimumRequiredOptions());
        log.info("ROTA CART SERVICE - TERMINANDO DE PROCESSAR CARRINHO: {}", cart);
        return cartRepository.save(cart);
    }

    @CachePut(value = "cart", key = "#cartCode")
    @Transactional
    public Cart incrementItem(String cartCode, Long productId, String codeProduct) {
        log.info("ROTA CART SERVICE - INCREMENT - INCREMENTANDO PRODUTO AO CARRINHO: {} - {} - {} ", cartCode, productId, codeProduct);
        // Acha o carrinho pelo código
        Cart cart = getCartByCartCode(cartCode);
        log.info("ROTA CART SERVICE - INCREMENT - ACHANDO CARRINHO PELO CÓDIGO: {}", cart);

        // Acha o produto no sistema
        Product product = productRepository.findByCodeProduct(codeProduct)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado no sistema: " + productId));
        log.info("ROTA CART SERVICE - INCREMENT - ACHANDO PRODUTO PELO CÓDIGO: {}", product);

        // Acha o item no carrinho comparando o código do produto (codeProduct)
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> {
                    // Verificar o código do produto e o ID para garantir que estamos comparando o item correto
                    Product cartProduct = item.getProduct();
                    return cartProduct != null && codeProduct.equals(cartProduct.getCodeProduct())
                            && product.getId().equals(cartProduct.getId());  // Comparar também pelo ID, se aplicável
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Produto do carrinho não encontrado ou não coincide com o sistema"));
        log.info("ROTA CART SERVICE - INCREMENT - COMPARANDO PRODUTOS DO SISTEMA E DO CARRINHO: {}", cartItem);

        // Incrementa a quantidade do item no carrinho
        cartItem.setQuantity(cartItem.getQuantity() + 1);
        log.info("ROTA CART SERVICE - INCREMENT - INCREMENTOU O PRODUTO: {}", cartItem);

        // Atualiza o total do item e do carrinho
        updateCartItemTotal(cartItem);
        cart.calculateTotal();

        // Valida os itens do carrinho e o estado do carrinho
        validateCartItem(cart, cartItem.getProduct().getCodeProduct(), product.getGetMinimumRequiredOptions());
        cart.setStatus(CartStatus.PROCESSING);

        log.info("ROTA CART SERVICE - INCREMENT - CARRINHO A SER SALVO: {}", cart);
        // Salva e retorna o carrinho atualizado
        return cartRepository.save(cart);
    }

    @CachePut(value = "cart", key = "#cartCode")
    @Transactional
    public Cart decrementItem(String cartCode, Long productId, String codeProduct) {
        log.info("ROTA CART SERVICE - DECREMENT - DECREMENTANDO PRODUTO AO CARRINHO: {} - {} - {} ", cartCode, productId, codeProduct);

        Cart cart = getCartByCartCode(cartCode);
        log.info("ROTA CART SERVICE - DECREMENT - ACHANDO CARRINHO PELO CÓDIGO: {}", cart);

        // Acha o produto no sistema
        Product product = productRepository.findByCodeProduct(codeProduct)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado no sistema: " + codeProduct));
        log.info("ROTA CART SERVICE - DECREMENT - ACHANDO PRODUTO PELO CÓDIGO: {}", product);

        // Acha o item no carrinho comparando o código do produto (codeProduct)
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> {
                    // Verificar o código do produto e o ID para garantir que estamos comparando o item correto
                    Product cartProduct = item.getProduct();
                    return cartProduct != null && codeProduct.equals(cartProduct.getCodeProduct())
                            && product.getId().equals(cartProduct.getId());  // Comparar também pelo ID, se aplicável
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Produto do carrinho não encontrado ou não coincide com o sistema"));
        log.info("ROTA CART SERVICE - INCREMENT - COMPARANDO PRODUTOS DO SISTEMA E DO CARRINHO: {}", cartItem);

        if (cartItem.getQuantity() > 1) {
            cartItem.setQuantity(cartItem.getQuantity() - 1);
            updateCartItemTotal(cartItem);
            cartItemRepository.save(cartItem);
        } else {
            cartItemRepository.delete(cartItem);
            cart.getItems().remove(cartItem);
        }

        if (cart.getItems().isEmpty()) {
            cart.setTotal(BigDecimal.ZERO);
            cart.setStatus(CartStatus.EMPTY);
        } else {
            cart.calculateTotal();
        }

        cart.setStatus(CartStatus.PROCESSING);

        log.info("ROTA CART SERVICE - DECREMENT - CARRINHO A SER SALVO: {}", cart);
        return cart;
    }

    @CachePut(value = "cart", key = "#cartCode")
    @Transactional
    public Cart incrementOptionQuantity(String cartCode, String codeProduct, String codeOption) {
        log.info("ROTA CART SERVICE - INCREMENTANDO OPÇÃO DO PRODUTO: {} - {} - {} ", cartCode, codeProduct, codeProduct);

        // Obtenha o carrinho pelo código
        Cart cart = getCartByCartCode(cartCode);
        log.info("ROTA CART SERVICE - INCREMENTANDO OPÇÃO DO PRODUTO - ACHANDO CARRINHO PELO CÓDIGO: {}", cart);

        // Verifique se a opção existe no produto
        Product product = productRepository.findByCodeProduct(codeProduct)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + codeProduct));
        log.info("ROTA CART SERVICE - INCREMENTANDO OPÇÃO DO PRODUTO - ACHANDO PRODUTO PELO CÓDIGO: {}", product);

        // Verifique se o item do carrinho existe
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getCodeProduct().equals(codeProduct))
                .findFirst()
                .orElse(null);
        log.info("ROTA CART SERVICE - INCREMENTANDO OPÇÃO DO PRODUTO - COMPARANDO PRODUTOS DO CARRINHO E DO SISTEMA: {}", cartItem);

        // Se o item não existir, adicione ao carrinho e recupere o item adicionado
        if (cartItem == null) {
            cart = addItem(cart.getMerchantCode(), cartCode, product.getCodeProduct()); // Atualiza o carrinho com o novo item
            // Recupera o item do carrinho atualizado
            cartItem = cart.getItems().stream()
                    .filter(item -> item.getProduct().getCodeProduct().equals(codeProduct))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Falha ao adicionar o item ao carrinho: " + codeProduct));
            log.info("ROTA CART SERVICE - INCREMENTANDO OPÇÃO DO PRODUTO - ITEM ADICIONADO AO CARRINHO: {}", cartItem);
        }

        // Verifique se a opção existe no produto
        ProductItem productOption = product.getItems().stream()
                .filter(item -> codeOption.equals(item.getCodeOption()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Opção do produto não encontrada: " + codeOption));
        log.info("ROTA CART SERVICE - INCREMENTANDO OPÇÃO DO PRODUTO - ACHANDO OPÇÕES DO PRODUTO: {}", productOption);

        // Tente encontrar uma opção existente com o mesmo cartItemId e codeOption
        CartItemOption existingOption = cartItemOptionRepository.findByCartItemIdAndCodeOption(cartItem.getId(), codeOption);
        log.info("ROTA CART SERVICE - INCREMENTANDO OPÇÃO DO PRODUTO - ENCONTRANDO OPÇÃO EXISTENTE: {}", existingOption);

        if (existingOption != null) {
            // Se a opção já existe, apenas atualize a quantidade
            existingOption.setQuantity(existingOption.getQuantity() + 1);
            log.info("ROTA CART SERVICE - INCREMENTANDO OPÇÃO DO PRODUTO - ACRESCENTANDO +1: {}", existingOption);
            cartItemOptionRepository.save(existingOption);
        } else {
            // Se a opção não existe, crie uma nova
            CartItemOption newOption = new CartItemOption();
            newOption.setCartItem(cartItemRepository.findById(cartItem.getId()).orElseThrow());
            newOption.setCodeOption(codeOption);
            newOption.setName(productOption.getName());
            newOption.setPrice(productOption.getAdditionalPrice());
            newOption.setQuantity(1);
            log.info("ROTA CART SERVICE - INCREMENTANDO OPÇÃO DO PRODUTO - NOVA OPÇÃO ADICIONADA AO PRODUTO: {}", newOption);
            cartItemOptionRepository.save(newOption);
        }

        updateCartItemTotal(cartItem);
        cart.calculateTotal();
        validateCartItem(cart, cartItem.getProduct().getCodeProduct(), product.getGetMinimumRequiredOptions());
        cart.setStatus(CartStatus.PROCESSING);
        log.info("ROTA CART SERVICE - INCREMENTANDO OPÇÃO DO PRODUTO - CARRINHO ATUALIZADO: {}", cart);
        return cartRepository.save(cart);
    }

    @CachePut(value = "cart", key = "#cartCode")
    @Transactional
    public Cart decrementOptionQuantity(String cartCode, String codeProduct, String codeOption) {
        log.info("ROTA CART SERVICE - DECREMENTANDO OPÇÃO DO PRODUTO: {} - {} - {} ", cartCode, codeProduct, codeProduct);

        Cart cart = getCartByCartCode(cartCode);
        log.info("ROTA CART SERVICE - DECREMENTANDO OPÇÃO DO PRODUTO - ACHANDO CARRINHO PELO CÓDIGO: {}", cart);

        // Verifique se o produto existe
        Product product = productRepository.findByCodeProduct(codeProduct)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + codeProduct));
        log.info("ROTA CART SERVICE - DECREMENTANDO OPÇÃO DO PRODUTO - ACHANDO PRODUTO PELO CÓDIGO: {}", product);

        // Verifique se a opção existe no produto
        ProductItem productOption = product.getItems().stream()
                .filter(item -> codeOption.equals(item.getCodeOption()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Opção do produto não encontrada: " + codeOption));
        log.info("ROTA CART SERVICE - DECREMENTANDO OPÇÃO DO PRODUTO - ACHANDO OPÇÕES DO PRODUTO: {}", productOption);

        // Verifique se o item do carrinho existe
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getCodeProduct().equals(codeProduct))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item do carrinho não encontrado para o produto: " + codeProduct));
        log.info("ROTA CART SERVICE - DECREMENTANDO OPÇÃO DO PRODUTO - COMPARANDO PRODUTOS DO CARRINHO E DO SISTEMA: {}", cartItem);

        // Tente encontrar uma opção existente com o mesmo cartItemId e codeOption
        CartItemOption existingOption = cartItemOptionRepository.findByCartItemIdAndCodeOption(cartItem.getId(), codeOption);
        log.info("ROTA CART SERVICE - DECREMENTANDO OPÇÃO DO PRODUTO - ENCONTRANDO OPÇÃO EXISTENTE: {}", existingOption);

        if (existingOption != null) {
            if (existingOption.getQuantity() > 1) {
                // Se a quantidade for maior que 1, apenas decrementa e salva
                existingOption.setQuantity(existingOption.getQuantity() - 1);
                cartItemOptionRepository.save(existingOption);
            } else {
                // Se a quantidade chegar a 1, remova a opção
                cartItemOptionRepository.delete(existingOption);

                // Remova também da lista de opções do CartItem para evitar referências futuras
                cartItem.getOptions().remove(existingOption);
            }
        } else {
            throw new RuntimeException("Opção do carrinho não encontrada: " + codeOption);
        }

        // Atualize o total do item do carrinho e o total do carrinho
        updateCartItemTotal(cartItem);
        cart.calculateTotal();

        // Valide o carrinho
        validateCartItem(cart, cartItem.getProduct().getCodeProduct(), product.getGetMinimumRequiredOptions());
        cart.setStatus(CartStatus.PROCESSING);

        log.info("ROTA CART SERVICE - DECREMENTANDO OPÇÃO DO PRODUTO - CARRINHO ATUALIZADO: {}", cart);
        return cartRepository.save(cart);
    }

    @CacheEvict(value = "cart", key = "#cartCode")
    public Cart removeItem(String cartCode, String codeProduct) {
        log.info("ROTA CART SERVICE - REMOVENDO PRODUTO - DADOS RECEBIDOS: {} - {}", cartCode, codeProduct);
        Cart cart = getCartByCartCode(cartCode);
        log.info("ROTA CART SERVICE - REMOVENDO PRODUTO - CARRINHO ACHADO: {}", cart);

        // Acha o item no carrinho comparando o código do produto (codeProduct)
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> {
                    // Verificar o código do produto e o ID para garantir que estamos comparando o item correto
                    Product cartProduct = item.getProduct();
                    return cartProduct != null && codeProduct.equals(cartProduct.getCodeProduct());
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Produto do carrinho não encontrado ou não coincide com o sistema"));

        cartItemRepository.delete(cartItem);
        cart.getItems().remove(cartItem);

        if (cart.getItems().isEmpty()) {
            cart.setTotal(BigDecimal.ZERO);
            cart.setStatus(CartStatus.EMPTY);
        } else {
            cart.calculateTotal();
        }

        cart.setStatus(CartStatus.PROCESSING);
        log.info("ROTA CART SERVICE - REMOVENDO PRODUTO - CARRINHO ATUALIZADO: {}", cart);
        return cartRepository.save(cart);
    }

    @CacheEvict(value = "cart", key = "#cartCode")
    public Cart clearCart(String cartCode) {
        Cart cart = getCartByCartCode(cartCode);
        cart.getItems().clear();
        cart.setTotal(BigDecimal.ZERO);
        cart.setStatus(CartStatus.CLEAR);
        return cartRepository.save(cart);
    }

    @CacheEvict(value = "cart", key = "#cartCode")
    public void deleteCart(String cartCode) {
        Cart cart = getCartByCartCode(cartCode);
        cartItemRepository.deleteAll(cart.getItems());
        cartRepository.delete(cart);
    }

    public void insertObservationInCart(String cartCode, String codeProduct, String observation) {
        Cart cart = getCartByCartCode(cartCode);

        if (cart == null) {
            throw new RuntimeException("Carrinho não encontrado para o código: " + cartCode);
        }

        Product product = productRepository.findByCodeProduct(codeProduct)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado no sistema: " + codeProduct));
        log.info("ROTA CART SERVICE - PRODUTO ENCONTRADO PELO CÓDIGO: {}", product);

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> {
                    Product cartProduct = item.getProduct();
                    return cartProduct != null && codeProduct.equals(cartProduct.getCodeProduct());
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Produto do carrinho não encontrado ou não coincide com o sistema"));
        log.info("ROTA CART SERVICE - PRODUTO NO CARRINHO ENCONTRADO: {}", cartItem);

        cartItem.setObservation(observation);
        log.info("ROTA CART SERVICE - INSERINDO OBSERVAÇÃO NO ITEM: {} - OBSERVAÇÃO: {}", cartItem, observation);

        cart.setStatus(CartStatus.PROCESSING);
        cartItemRepository.save(cartItem);
        log.info("ROTA CART SERVICE - ITEM SALVO COM A OBSERVAÇÃO: {}", cartItem);
    }

    private void updateCartItemTotal(CartItem cartItem) {
        BigDecimal optionsTotalPrice = cartItem.getOptions().stream()
                .map(option -> option.getPrice().multiply(BigDecimal.valueOf(option.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal additionalOptionsTotalPrice = cartItem.getAdditionalOptions().stream()
                .map(option -> option.getPrice().multiply(BigDecimal.valueOf(option.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cartItem.setTotalPrice(
                cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()))
                        .add(optionsTotalPrice)
                        .add(additionalOptionsTotalPrice)
        );

        cartItemRepository.save(cartItem);
    }

    public void validateCartItem(Cart cart, String codeProduct, Integer minimumRequiredOptions) {
        // Percorre os itens do carrinho para encontrar o item com o ID do produto
        for (CartItem cartItem : cart.getItems()) {
            // Verifica se o produto corresponde ao ID fornecido
            if (cartItem.getProduct().getCodeProduct().equals(codeProduct)) {
                // Inicializa as opções como uma lista vazia se for null
                List<CartItemOption> options = Optional.ofNullable(cartItem.getOptions())
                        .orElse(Collections.emptyList());

                // Calcula a quantidade total das opções
                int totalOptionQuantity = options.stream()
                        .mapToInt(CartItemOption::getQuantity)
                        .sum();

                // Define o status do item com base na quantidade de opções e no mínimo requerido
                if (minimumRequiredOptions > 0) {
                    if (totalOptionQuantity < minimumRequiredOptions) {
                        cartItem.setStatus(CartItemStatus.BLOCKED);
                    } else {
                        cartItem.setStatus(CartItemStatus.RELEASED);
                    }
                } else {
                    // Se não há opções mínimas obrigatórias, o item deve ser liberado
                    cartItem.setStatus(CartItemStatus.RELEASED);
                }

                // Salva o CartItem com o novo status
                cartItemRepository.save(cartItem);
                break;  // Sai do loop após encontrar e validar o item
            }
        }
    }

    public void insertCartItemInCart(String merchantCode ,String cartCode, String codeProduct){
        Cart cart = getCartByCartCode(cartCode);
        log.info("ROTA CART SERVICE - (NOVO) - INSERINDO PRODUTO NO CARRINHO: cartCode={} - codeProduct={}", cartCode, codeProduct);

        // Verifique se o item do carrinho existe
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getCodeProduct().equals(codeProduct))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item do carrinho não encontrado para o produto: " + codeProduct));
        log.info("ROTA CART SERVICE - (NOVO) - : cartItem={}", cartItem);

        //Mudar o status do produto para liberado.
        cartItem.setStatus(CartItemStatus.RELEASED);
        cartRepository.save(cart);
    }

    public void updateStatusCart(CartStatus status, String cartCode){
        Cart cart = getCartByCartCode(cartCode);
        cart.setStatus(status);
        cartRepository.save(cart);
    }

}
