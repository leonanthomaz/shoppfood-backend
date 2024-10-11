package com.microshoppe.erp.product.controller;

import com.microshoppe.erp.common.dto.MerchantCodeDTO;
import com.microshoppe.erp.product.dto.ImageUpdateRequestDTO;
import com.microshoppe.erp.product.dto.ProductDTO;
import com.microshoppe.erp.product.exception.ProductException;
import com.microshoppe.erp.product.model.Category;
import com.microshoppe.erp.product.model.Product;
import com.microshoppe.erp.product.service.CategoryService;
import com.microshoppe.erp.product.service.ProductService;
import com.microshoppe.erp.store.model.Store;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    // ******** ROTA DE ADMINISTRADOR - SUPER *********
    //PEGAR TODOS OS PRODUTOS *********
    @Cacheable(value = "allProducts")
    @Secured({"ROLE_SYSTEM_ADMIN", "ROLE_ADMIN"})
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        log.info("ROTA PRODUTO >>> OBTENDO TODOS OS PRODUTOS");
        try {
            List<Product> products = productService.getAllProducts();
            return ResponseEntity.status(HttpStatus.OK).body(products);
        } catch (Exception e) {
            log.error("ROTA PRODUTO >>> Erro inesperado ao recuperar todos os produtos", e);
            throw new ProductException("Nenhum produto encontrado para o código de Loja e pelo código do cliente fornecido.");
        }
    }

    // ******** ROTA DE ADMINISTRADOR - ADMIN *********
    //PEGAR PRODUTO PELO CODIGO DA LOJA *********
    @Cacheable(value = "productsByMerchant", key = "#data.merchantCode")
    @PostMapping("/store")
    public ResponseEntity<List<Product>> getProductsByMerchantCode(@RequestBody MerchantCodeDTO data) {
        log.info("ROTA PRODUTO >>> PEGANDO PRODUTOS POR CODIGO/CLIENTE: {}", data.getMerchantCode());
        try {
            List<Product> products = productService.getProductsByMerchantCode(data.getMerchantCode());
            if (products != null && !products.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body(products);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (ProductException e) {
            log.error("ROTA PRODUTO >>> Erro ao recuperar produtos por código de cliente", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("Erro inesperado ao recuperar produtos", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    //PEGAR PRODUTO PELO ID DO PRODUTO E CODIGO DA LOJA *********
    @PostMapping("/{productId}/store")
    public ResponseEntity<Product> getProductByIdAndMerchantCode(@PathVariable Long productId, @RequestBody MerchantCodeDTO data) {
        log.info("ROTA PRODUTO >>> OBTENDO PRODUTO POR ID E CODIGO/CLIENTE: {} - {}", productId, data.getMerchantCode());
        try {
            Product product = productService.getProductByIdAndMerchantCode(productId, data.getMerchantCode());
            if (product != null) {
                return ResponseEntity.status(HttpStatus.OK).body(product);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception e) {
            log.error("ROTA PRODUTO >>> Erro ao recuperar produto por ID e código de cliente", e);
            throw new ProductException("Nenhum produto encontrado para o código de Loja e pelo ID do cliente fornecido.");
        }
    }

    //PEGAR PRODUTO PELO CÓDIGO DO PRODUTO DO PRODUTO E CODIGO DA LOJA *********
    @Cacheable(value = "productByCodeAndMerchant", key = "{#codeProduct, #data.merchantCode}")
    @PostMapping("/find/{codeProduct}")
    public ResponseEntity<Product> getProductByCodeProductAndMerchantCode(@PathVariable String codeProduct, @RequestBody MerchantCodeDTO data) {
        log.info("ROTA PRODUTO >>> OBTENDO PRODUTO POR CÓDIGO/PRODUTO E CODIGO/CLIENTE: {} - {}", codeProduct, data.getMerchantCode());
        try {
            Product product = productService.getProductByCodeProductAndMerchantCode(codeProduct, data.getMerchantCode());
            if (product != null) {
                return ResponseEntity.status(HttpStatus.OK).body(product);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception e) {
            log.error("ROTA PRODUTO >>> Erro ao recuperar produto por ID e código de cliente", e);
            throw new ProductException("Nenhum produto encontrado para o código de Loja e pelo código do cliente fornecido.");
        }
    }

    // ********** CRUD **************
    //CRIAR PRODUTO
    @Secured("ROLE_ADMIN")
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody ProductDTO product) {
        log.info("ROTA PRODUTO >>> CRIANDO UM PRODUTO: {}", product);
        try {
            Product createdProduct = productService.createProduct(product, product.getMerchantCode());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
        } catch (IllegalArgumentException e) {
            log.error("ROTA PRODUTO >>> Erro ao criar produto: Merchant code inválido", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("ROTA PRODUTO >>> Erro ao criar produto", e);
            throw new ProductException("Erro ao criar produto.");
        }
    }

    //ATUALIZAR PRODUTO
    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDetails) {
        log.info("ROTA PRODUTO >>> ATUALIZANDO UM PRODUTO: {}", id);
        try {
            Product updatedProduct = productService.updateProduct(id, productDetails, productDetails.getMerchantCode());
            if (updatedProduct != null) {
                return ResponseEntity.status(HttpStatus.OK).body(updatedProduct);
            } else {
                log.warn("ROTA PRODUTO >>> Produto não encontrado ou merchant code incompatível");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (IllegalArgumentException e) {
            log.error("ROTA PRODUTO >>> Erro ao atualizar produto", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("ROTA PRODUTO >>> Erro ao atualizar produto", e);
            throw new ProductException("Erro ao atualizar produto.");
        }
    }

    //DELETAR PRODUTO
    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}")
    @CacheEvict(value = {"productsByMerchant", "productByIdAndMerchant", "productByCodeAndMerchant", "allProducts"}, allEntries = true)
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id, @RequestBody MerchantCodeDTO data) {
        log.info("ROTA PRODUTO >>> DELETANDO UM PRODUTO: {}", id);
        try {
            productService.deleteProductById(id, data.getMerchantCode());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            log.error("ROTA PRODUTO >>> Erro ao deletar produto: Merchant code incompatível ou produto não encontrado", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("ROTA PRODUTO >>> Erro ao deletar produto", e);
            throw new ProductException("Erro ao deletar produto.");
        }
    }


    // ******** ROTA DE ADMINISTRADOR - COMUM *********
    @PostMapping("/store/find")
    public ResponseEntity<Product> getProductByCodeProduct(@PathVariable String codeProduct) {
        log.info("ROTA PRODUTO >>> NOVO OBTENDO PRODUTO POR CODIGO >>> : {}", codeProduct);
        try {
            Product product = productService.getProductByProductCode(codeProduct);
            if (product != null) {
                return ResponseEntity.status(HttpStatus.OK).body(product);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception e) {
            log.error("ROTA PRODUTO >>> Erro ao recuperar produto por código", e);
            throw new ProductException("Piru.");
        }
    }

    //PEGAR PRODUTOS POR ID *********
    @Secured({"ROLE_SYSTEM_ADMIN", "ROLE_ADMIN"})
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        log.info("ROTA PRODUTO >>> OBTENDO PRODUTO POR ID: {}", id);
        try {
            Product product = productService.getProductById(id);
            if (product != null) {
                return ResponseEntity.status(HttpStatus.OK).body(product);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            log.error("ROTA PRODUTO >>> Erro inesperado ao recuperar produto por ID", e);
            throw new ProductException("Nenhum produto encontrado para o ID informado.");
        }
    }

    //ADICIONAR CATEGORIA NO PRODUTO *********
    @Secured("ROLE_ADMIN")
    @PostMapping("/add-to-category")
    public ResponseEntity<Product> addProductToCategory(
            @RequestParam Long productId,
            @RequestParam Long categoryId,
            @RequestBody MerchantCodeDTO data) {
        log.info(" ROTA PRODUTO >>> ADICIONAR CATEGORIA: {} - {} - {}", productId, categoryId, data);

        try {
            // Verificar se o produto e a categoria existem
            Product product = productService.getProductByIdAndMerchantCode(productId, data.getMerchantCode());
            Category category = categoryService.findByIdAndMerchantCode(categoryId, data.getMerchantCode());

            // Associar a categoria ao produto
            product.setCategory(category);
            Product updatedProduct = productService.updateProduct(product);

            return ResponseEntity.status(HttpStatus.OK).body(updatedProduct);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            throw new ProductException("Erro ao adicionar categoria ao produto.");
        }
    }

    //RECEBER IMAGEM, ARMAZENAR E INSERIR NO BANCO DE DADOS
    @PostMapping("/{productId}/upload-and-update-image")
    public ResponseEntity<String> uploadAndUpdateProductImage(
            @PathVariable("productId") Long productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("merchantCode") String merchantCode) {
        log.info(" ROTA PRODUTO >>> ADICIONAR E ATUALIZAR IMAGEM: {} - {} - {}", productId, file, merchantCode);

        try {
            // Valida o tamanho da imagem (máximo 1MB)
            if (file.getSize() > 1048576) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A imagem excede o tamanho máximo permitido de 1MB.");
            }

            // Define o caminho do diretório onde as imagens serão armazenadas
            String projectDir = System.getProperty("user.dir");
            Path uploadDir = Paths.get(projectDir, "src", "main", "resources", "uploads", "products");

            // Cria o diretório se ele não existir
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Gera um UUID e define o nome do arquivo
            String uuid = UUID.randomUUID().toString();
            String fileName = uuid + "_" + file.getOriginalFilename();
//            String fileName = uuid + "_" + merchantCode;
            Path filePath = uploadDir.resolve(fileName);

            // Salva o arquivo
            Files.write(filePath, file.getBytes());

            log.info("ROTA PRODUTO >>> UPLOAD filePath: {}", filePath);

            // Atualiza a URL da imagem no produto
            String imageUrl = fileName;  // Aqui, consideramos o nome do arquivo como a URL da imagem
            productService.updateProductImage(productId, merchantCode, imageUrl);

            return ResponseEntity.ok("Imagem do produto atualizada com sucesso. Nome da imagem: " + fileName);
        } catch (IOException e) {
            log.error("Failed to upload image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            throw new ProductException("Erro ao atualizar imagem do produto.");
        }
    }

    //PRECISO FORMULAR ESSA ROTA DE ATUALIZAÇÃO DE IMAGEM
    @PostMapping("/{productId}/update-image")
    public ResponseEntity<String> updateProductImage(
            @PathVariable Long productId,
            @RequestBody ImageUpdateRequestDTO data) {

        try {
            productService.updateProductImage(productId, data.getMerchantCode(), data.getImageUrl());
            return ResponseEntity.ok("Imagem do produto atualizada com sucesso");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            throw new ProductException("Erro ao atualizar imagem do produto.");
        }
    }

    //CAPTURAR A IMAGEM DO PRODUTO NO SISTEMA
    @Cacheable(value = "productImages", key = "#filename")
    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        log.info("ROTA PRODUTO >>>  RECUPERANDO IMAGEM: {}", filename);

        try {
            // Define o caminho do diretório onde as imagens são armazenadas
            String projectDir = System.getProperty("user.dir");
            Path filePath = Paths.get(projectDir, "src", "main", "resources", "uploads",  "products", filename);

            Resource resource = new FileSystemResource(filePath.toFile());

            if (resource.exists()) {
                log.info("ROTA PRODUTO >>>  Imagem encontrada no cache ou disco: {}", filename);
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG) // Ajuste conforme o tipo de imagem
                        .body(resource);
            } else {
                log.warn("ROTA PRODUTO >>>  Imagem não encontrada: {}", filename);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            log.error("ROTA PRODUTO >>> Erro ao recuperar imagem", e);
            throw new ProductException("Erro ao atualizar imagem do produto.");
        }
    }

    @PutMapping("/active")
    public ResponseEntity<Product> activeStore(
            @RequestParam("productId") Long productId,
            @RequestParam("condition") boolean condition,
            @RequestParam("merchantCode") String merchantCode
    ){
        try {
            log.info("PRODUTO >>> ATUALIZAÇÃO DE STATUS DA LOJA >>> DADOS RECEBIDOS: {} - {} - {}", productId, condition, merchantCode);
            Product activeProduct = productService.activeProduct(productId, condition, merchantCode);
            return ResponseEntity.ok(activeProduct);
        } catch (IllegalArgumentException error) { // Exceção mais específica
            log.error("PRODUTO >>> ATUALIZAÇÃO DE STATUS DA LOJA: {}", error.getMessage());
            return ResponseEntity.badRequest().body(null); // Retornando uma resposta adequada
        } catch (RuntimeException error) {
            log.error("Erro inesperado ao atualizar loja: {}", error.getMessage());
            return ResponseEntity.status(500).body(null); // Usar status 500 para erro genérico
        }
    }
}
