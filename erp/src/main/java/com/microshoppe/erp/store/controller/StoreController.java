package com.microshoppe.erp.store.controller;

import com.microshoppe.erp.store.dto.StoreDTO;
import com.microshoppe.erp.store.dto.StoreUpdateDTO;
import com.microshoppe.erp.store.exception.StoreException;
import com.microshoppe.erp.store.model.Store;
import com.microshoppe.erp.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @Secured("SYSTEM_ADMIN")
    @PostMapping("/create")
    public ResponseEntity<Store> createNewStore(@RequestBody @Valid StoreDTO data) {
        try {
            log.info("STORE >>> CRIAÇÃO DE LOJA >>> DADOS RECEBIDOS: {}", data);
            Store newStore = storeService.createNewStore(data);
            return ResponseEntity.status(HttpStatus.CREATED).body(newStore);
        } catch (RuntimeException error) {
            log.error("STORE >>> FALHA AO CRIAR LOJA: {}", error.getMessage());
            throw new RuntimeException("Falha ao criar Loja");
        }
    }

    @PutMapping("/active")
    public ResponseEntity<Store> activeStore(@RequestParam("merchantCode") String merchantCode,
                                             @RequestParam("condition") boolean condition){
        try {
            log.info("STORE >>> ATUALIZAÇÃO DE STATUS DA LOJA >>> DADOS RECEBIDOS: {} - {}", merchantCode, condition);
            Store activeStore = storeService.activeStore(merchantCode, condition);
            return ResponseEntity.ok(activeStore);
        } catch (RuntimeException error) {
            log.error("STORE >>> ATUALIZAÇÃO DE STATUS DA LOJA: {}", error.getMessage());
            throw new RuntimeException("Falha ao atualizar loja");
        }
    }
    @Secured({"SYSTEM_ADMIN", "ADMIN"})
    @PutMapping("/edit")
    public ResponseEntity<Store> updateStore(
            @RequestBody @Valid StoreUpdateDTO data) {
        try {
            log.info("STORE >>> ATUALIZAÇÃO DE LOJA >>> DADOS RECEBIDOS: {}", data);
            Store updatedStore = storeService.updateStore(data);
            return ResponseEntity.ok(updatedStore);
        } catch (RuntimeException error) {
            log.error("STORE >>> FALHA AO ATUALIZAR LOJA: {}", error.getMessage());
            throw new RuntimeException("Falha ao atualizar loja");
        }
    }

    @GetMapping("/find")
    public Store getStoreByMechantCode(@RequestParam String merchantCode){
        try{
            log.info("STORE >>> RECUPERAR LOJA >>> DADOS RECEBIDOS: {}", merchantCode);
            return storeService.findByMerchantCode(merchantCode);
        } catch (RuntimeException error){
            throw new RuntimeException("Falha ao recuperar loja");
        }
    }

    @PostMapping("/upload-logo")
    public ResponseEntity<String> uploadStoreLogo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("merchantCode") String merchantCode) {
        try{
            return uploadStoreLogo(file, merchantCode, "logo");
        } catch (RuntimeException error){
            throw new RuntimeException("Falha ao processar imagem - logo");
        }
    }

    @PostMapping("/upload-header")
    public ResponseEntity<String> uploadStoreHeaderImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("merchantCode") String merchantCode) {
        try{
            return uploadStoreHeader(file, merchantCode, "header");
        } catch (RuntimeException error){
            throw new RuntimeException("Falha ao processar imagem - header");
        }
    }

    private ResponseEntity<String> uploadStoreLogo(MultipartFile file, String merchantCode, String imageType) {
        log.info("ROTA LOJA >>> ADICIONAR IMAGEM {} E ATUALIZAR: {} - {}", imageType, file, merchantCode);

        try {
            // Valida o tamanho da imagem (máximo 1MB)
            if (file.getSize() > 1048576) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A imagem excede o tamanho máximo permitido de 1MB.");
            }

            // Define o caminho do diretório onde as imagens serão armazenadas
            String projectDir = System.getProperty("user.dir");
            Path uploadDir = Paths.get(projectDir, "src", "main", "resources", "uploads", "store", imageType);

            // Cria o diretório se ele não existir
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Gera um UUID e define o nome do arquivo
            String uuid = UUID.randomUUID().toString();
            String fileName = uuid + "_" + file.getOriginalFilename();
            Path filePath = uploadDir.resolve(fileName);

            // Salva o arquivo
            Files.write(filePath, file.getBytes());

            log.info("ROTA LOJA >>> UPLOAD filePath: {}", filePath);

            // Atualiza a URL da imagem no Store
            storeService.updateStoreLogo(merchantCode, fileName);

            return ResponseEntity.ok("Imagem " + imageType + " da loja atualizada com sucesso. Nome da imagem: " + fileName);
        } catch (IOException e) {
            log.error("Falha ao fazer upload da imagem", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Falha ao fazer upload da imagem.");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar imagem da loja.");
        }
    }

    private ResponseEntity<String> uploadStoreHeader(MultipartFile file, String merchantCode, String imageType) {
        log.info("ROTA LOJA >>> ADICIONAR IMAGEM {} E ATUALIZAR: {} - {}", imageType, file, merchantCode);

        try {
            // Valida o tamanho da imagem (máximo 1MB)
            if (file.getSize() > 1048576) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A imagem excede o tamanho máximo permitido de 1MB.");
            }

            // Define o caminho do diretório onde as imagens serão armazenadas
            String projectDir = System.getProperty("user.dir");
            Path uploadDir = Paths.get(projectDir, "src", "main", "resources", "uploads", "store", imageType);

            // Cria o diretório se ele não existir
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Gera um UUID e define o nome do arquivo
            String uuid = UUID.randomUUID().toString();
            String fileName = uuid + "_" + file.getOriginalFilename();
            Path filePath = uploadDir.resolve(fileName);

            // Salva o arquivo
            Files.write(filePath, file.getBytes());

            log.info("ROTA LOJA >>> UPLOAD filePath: {}", filePath);

            // Atualiza a URL da imagem no Store
            storeService.updateStoreHeader(merchantCode, fileName);

            return ResponseEntity.ok("Imagem " + imageType + " da loja atualizada com sucesso. Nome da imagem: " + fileName);
        } catch (IOException e) {
            log.error("Falha ao fazer upload da imagem", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Falha ao fazer upload da imagem.");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar imagem da loja.");
        }
    }

    // CAPTURAR A IMAGEM DA LOGO DA LOJA
    @Cacheable(value = "storeLogos", key = "#merchantCode")
    @GetMapping("/store/logo/{merchantCode}/{filename}")
    public ResponseEntity<Resource> getStoreLogo(@PathVariable String merchantCode, @PathVariable String filename) {
        log.info("ROTA LOJA >>> RECUPERANDO LOGO DA LOJA: {}", merchantCode);

        try {
            // Define o caminho do diretório onde as logos são armazenadas
            String projectDir = System.getProperty("user.dir");
            Path filePath = Paths.get(projectDir, "src", "main", "resources", "uploads", "store", "logo", filename);

            Resource resource = new FileSystemResource(filePath.toFile());

            if (resource.exists()) {
                log.info("ROTA LOJA >>> Logo encontrada no cache ou disco: {}", merchantCode);
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG) // Ajuste conforme o tipo de imagem
                        .body(resource);
            } else {
                log.warn("ROTA LOJA >>> Logo não encontrada: {}", merchantCode);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            log.error("ROTA LOJA >>> Erro ao recuperar logo da loja", e);
            throw new StoreException("Erro ao atualizar logo da loja.");
        }
    }

    // CAPTURAR A IMAGEM DA LOGO DA LOJA
    @Cacheable(value = "storeLogos", key = "#merchantCode")
    @GetMapping("/store/header/{merchantCode}/{filename}")
    public ResponseEntity<Resource> getStoreHeader(@PathVariable String merchantCode, @PathVariable String filename) {
        log.info("ROTA LOJA >>> RECUPERANDO HEADER DA LOJA: {}", merchantCode);

        try {
            // Define o caminho do diretório onde as logos são armazenadas
            String projectDir = System.getProperty("user.dir");
            Path filePath = Paths.get(projectDir, "src", "main", "resources", "uploads", "store", "header", filename);

            Resource resource = new FileSystemResource(filePath.toFile());

            if (resource.exists()) {
                log.info("ROTA LOJA >>> Header encontrada no cache ou disco: {}", merchantCode);
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            } else {
                log.warn("ROTA LOJA >>> Header não encontrada: {}", merchantCode);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            log.error("ROTA LOJA >>> Erro ao recuperar header da loja", e);
            throw new StoreException("Erro ao atualizar header da loja.");
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteStore(@PathVariable String merchantCode){
        try{
            storeService.deleteStore(merchantCode);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Loja excluída com sucesso!");
        } catch (Exception error){
            throw new RuntimeException("Falha ao recuperar loja");
        }
    }

    @PutMapping("/open/{merchantCode}/{status}")
    public ResponseEntity<?> openStore(@PathVariable String merchantCode, @PathVariable boolean status) {
        log.info("STORE >>> ABRIR/FECHAR LOJA >>> DADOS RECEBIDOS : status={} - merchantCode={}", status, merchantCode);
        try {
            Store store = storeService.findByMerchantCode(merchantCode);
            log.info("STORE >>> ABRIR/FECHAR LOJA >>> LOJA ENCONTRADA -> store={}", store);
            store.setOpen(status);
            log.info("STORE >>> ABRIR/FECHAR LOJA >>> LOJA ATUALIZADA -> store={}", store);
            storeService.saveStore(store);
            return ResponseEntity.ok("Sucesso!");
        } catch (RuntimeException error) {
            log.error("STORE >>> FALHA AO ABRIR/FECHAR LOJA >>> ", error);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Falha ao abrir/fechar a loja: " + error.getMessage());
        }
    }

}
