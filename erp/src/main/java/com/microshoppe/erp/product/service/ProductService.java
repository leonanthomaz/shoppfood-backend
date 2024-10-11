package com.microshoppe.erp.product.service;

import com.microshoppe.erp.authentication.repository.UserRepository;
import com.microshoppe.erp.product.dto.ProductDTO;
import com.microshoppe.erp.product.model.Category;
import com.microshoppe.erp.product.model.Product;
import com.microshoppe.erp.product.repository.ProductRepository;
import com.microshoppe.erp.store.model.Store;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;

    private String generateRandomProductCode() {
        String uuid = UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "");
        return uuid.substring(0, Math.min(uuid.length(), 8)).toUpperCase();
    }

    private boolean isMerchantCodeValid(String merchantCode) {
        return userRepository.existsByMerchantCode(merchantCode);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByMerchantCode(String merchantCode) {
        return productRepository.findByMerchantCode(merchantCode);
    }

    public Product getProductByIdAndMerchantCode(Long id, String merchantCode) {
        return productRepository.findById(id)
                .filter(product -> product.getMerchantCode().equals(merchantCode))
                .orElse(null);
    }

    public Product getProductByProductCode(String codeProduct) {
        return productRepository.findByCodeProduct(codeProduct)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }

    public Product getProductByCodeProductAndMerchantCode(String codeProduct, String merchantCode) {
        log.info("RECEBENDO A REQUISIÇÃO - PRODURANDO PRODUTO POR CODIGOS DO PRODUTO E CLIENTE: {} - {} ", codeProduct, merchantCode);
        return productRepository.findByCodeProduct(codeProduct)
                .filter(product -> product.getMerchantCode().equals(merchantCode))
                .orElse(null);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }

    public Product createProduct(ProductDTO product, String merchantCode) {
        if (!isMerchantCodeValid(merchantCode)) {
            throw new IllegalArgumentException("Merchant code is invalid");
        }

        Product newProduct = Product.builder()
                .codeProduct(generateRandomProductCode())
                .name(product.getName())
                .merchantCode(product.getMerchantCode())
                .description(product.getDescription())
                .price(product.getPrice())
                .getMinimumRequiredOptions(product.getGetMinimumRequiredOptions())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .codeBar(product.getCodeBar())
                .category(null)
                .build();

        // Salvar o produto
        Product savedProduct = productRepository.save(newProduct);

        // Se a categoria for fornecida, atualizar o produto com a categoria
        if (product.getCategoryId() != null) {
            Category category = categoryService.findById(product.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));

            savedProduct.setCategory(category);
            savedProduct = productRepository.save(savedProduct);
        }

        return savedProduct;
    }

    public Product updateProduct(Long id, ProductDTO productDetails, String merchantCode) {
        return productRepository.findById(id).map(product -> {
            if (!product.getMerchantCode().equals(merchantCode)) {
                throw new IllegalArgumentException("Merchant code mismatch");
            }
            product.setName(productDetails.getName());
            product.setDescription(productDetails.getDescription());
            product.setPrice(productDetails.getPrice());
            product.setImageUrl(productDetails.getImageUrl());
            product.setCodeBar(productDetails.getCodeBar());
            product.setGetMinimumRequiredOptions(productDetails.getGetMinimumRequiredOptions());
            product.setStock(productDetails.getStock());

            // Salva o produto atualizado
            return productRepository.save(product);
        }).orElse(null);
    }

    public Product updateProduct(Product product) {
        if (product.getId() == null || !productRepository.existsById(product.getId())) {
            throw new IllegalArgumentException("Product does not exist");
        }
        return productRepository.save(product);
    }

    public void updateProductImage(Long productId, String merchantCode, String imageUrl) {
        productRepository.findById(productId).map(product -> {
            if (!product.getMerchantCode().equals(merchantCode)) {
                throw new IllegalArgumentException("Merchant code mismatch");
            }
            // Atualiza o campo de imagem do produto
            product.setImageUrl(imageUrl);
            return productRepository.save(product);
        }).orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    public void deleteProductById(Long id, String merchantCode) {
        Product product = getProductById(id);
        if (product == null || !product.getMerchantCode().equals(merchantCode)) {
            throw new IllegalArgumentException("Product not found or merchant code mismatch");
        }
        productRepository.deleteById(id);
    }

    public Product activeProduct(Long productId, boolean condition, String merchantCode) {
        Product existingProduct = getProductById(productId);
        if (existingProduct == null) {
            throw new IllegalArgumentException("Produto não encontrado.");
        }
        if (!existingProduct.getMerchantCode().equals(merchantCode)) {
            throw new IllegalArgumentException("Código do comerciante não corresponde.");
        }
        existingProduct.setActive(condition);

        return productRepository.save(existingProduct); // Certifique-se de salvar as alterações no banco
    }

}
