package com.microshoppe.erp.product.controller;

import com.microshoppe.erp.common.dto.MerchantCodeDTO;
import com.microshoppe.erp.product.dto.ProductItemDTO;
import com.microshoppe.erp.product.model.Product;
import com.microshoppe.erp.product.service.ProductItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("/products/options/additional")
@RequiredArgsConstructor
public class ProductItemController {
    private final ProductItemService productItemService;

    @PostMapping("/{productId}/items")
    public ResponseEntity<Product> addItemToProduct(
            @PathVariable Long productId,
            @RequestBody ProductItemDTO itemDTO) {
        log.info("ENTRADA >>> ADICIONAR ITEM AO PRODUTO: {} - {}", productId, itemDTO.getMerchantCode());
        try {
            Product updatedProduct = productItemService.addItemToProduct(productId, itemDTO, itemDTO.getMerchantCode());
            return ResponseEntity.status(HttpStatus.OK).body(updatedProduct);
        } catch (Exception e) {
            log.error("Erro ao adicionar item ao produto", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{productId}/items/{itemId}")
    public ResponseEntity<Product> updateItemInProduct(
            @PathVariable Long productId,
            @PathVariable Long itemId,
            @RequestBody ProductItemDTO itemDTO) {
        log.info("ENTRADA >>> ATUALIZAR ITEM NO PRODUTO: {} - {}", productId, itemId);
        try {
            Product updatedProduct = productItemService.updateItemInProduct(productId, itemId, itemDTO, itemDTO.getMerchantCode());
            return ResponseEntity.status(HttpStatus.OK).body(updatedProduct);
        } catch (Exception e) {
            log.error("Erro ao atualizar item no produto", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{productId}/items/{itemId}")
    public ResponseEntity<Product> removeItemFromProduct(@PathVariable Long productId, @PathVariable Long itemId, @RequestBody MerchantCodeDTO data) {
        log.info("ENTRADA >>> REMOVER ITEM DO PRODUTO: {} - {}", productId, itemId);
        try {
            Product updatedProduct = productItemService.removeItemFromProduct(productId, itemId, data.getMerchantCode());
            return ResponseEntity.status(HttpStatus.OK).body(updatedProduct);
        } catch (Exception e) {
            log.error("Erro ao remover item do produto", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
