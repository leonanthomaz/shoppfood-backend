package com.microshoppe.erp.product.service;

import com.microshoppe.erp.authentication.repository.UserRepository;
import com.microshoppe.erp.product.dto.ProductItemDTO;
import com.microshoppe.erp.product.model.Product;
import com.microshoppe.erp.product.model.ProductItem;
import com.microshoppe.erp.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductItemService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private String generateRandomProductItemCode() {
        String uuid = UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "");
        return uuid.substring(0, Math.min(uuid.length(), 8)).toUpperCase();
    }

    public boolean isMerchantCodeValid(String merchantCode) {
        return userRepository.existsByMerchantCode(merchantCode);
    }

    public Product findById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public Product addItemToProduct(Long productId, ProductItemDTO itemDTO, String merchantCode) {
        if (!isMerchantCodeValid(merchantCode)) {
            throw new IllegalArgumentException("Merchant code is invalid");
        }

        return productRepository.findById(productId).map(product -> {
            ProductItem item = new ProductItem();
            item.setCodeOption(generateRandomProductItemCode());
            item.setName(itemDTO.getName());
            item.setMerchantCode(merchantCode);
            item.setAdditionalPrice(itemDTO.getAdditionalPrice());
            item.setProduct(product);
            product.getItems().add(item);
            return productRepository.save(product);
        }).orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    public Product updateItemInProduct(Long productId, Long itemId, ProductItemDTO itemDTO, String merchantCode) {
        if (!isMerchantCodeValid(merchantCode)) {
            throw new IllegalArgumentException("CÓDIGO DO CLIENTE INVALIDO");
        }

        return productRepository.findById(productId).map(product -> {
            ProductItem item = product.getItems().stream()
                    .filter(i -> i.getId().equals(itemId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("PRODUTO NAO ENCONTRADO PARA A OPÇÃO"));

            item.setName(itemDTO.getName());
            item.setAdditionalPrice(itemDTO.getAdditionalPrice());
            return productRepository.save(product);
        }).orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    public Product removeItemFromProduct(Long productId, Long itemId, String merchantCode) {
        if (!isMerchantCodeValid(merchantCode)) {
            throw new IllegalArgumentException("Merchant code is invalid");
        }

        return productRepository.findById(productId).map(product -> {
            ProductItem item = product.getItems().stream()
                    .filter(i -> i.getId().equals(itemId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Product item not found"));

            product.getItems().remove(item);
            return productRepository.save(product);
        }).orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

}
