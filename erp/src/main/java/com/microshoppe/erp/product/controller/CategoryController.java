package com.microshoppe.erp.product.controller;

import com.microshoppe.erp.common.dto.MerchantCodeDTO;
import com.microshoppe.erp.product.dto.CategoryDTO;
import com.microshoppe.erp.product.dto.CategoryRequestDTO;
import com.microshoppe.erp.product.model.Category;
import com.microshoppe.erp.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // FILTRA CATEGORIAS POR PARAMETRO FILTRO. SE NAO ACHAR, TRAZ TODAS AS CATEGORIAS
    @GetMapping("/find")
    public ResponseEntity<List<Category>> getCategoriesWithProducts(@RequestParam(required = false) String filter,
                                                                    @RequestParam String merchantCode) {
        log.info("ROTA CATEGORY >>> CATEGORIA POR FILTRO (RETORNA UMA LISTA): {}", filter);
        try {
            List<Category> categories;
            if (filter != null && !filter.isEmpty()) {
                categories = categoryService.findByNameContaining(filter);
            } else {
                categories = categoryService.findAllCategoriesByMerchantCode(merchantCode);
            }
            return ResponseEntity.status(HttpStatus.OK).body(categories);
        } catch (Exception e) {
            log.error("ROTA CATEGORY >>> Erro ao recuperar categorias", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/find/store")
    public ResponseEntity<List<Category>> getCategoriesWithMerchantCode(@RequestBody CategoryRequestDTO data) {
        log.info("ROTA CATEGORY >>> CATEGORIA POR CÓDIGO DE USUARIO (RETORNA UMA LISTA): {}", data.getMerchantCode());
        try {
            List<Category> categories;
            categories = categoryService.findAllCategoriesByMerchantCode(data.getMerchantCode());
            return ResponseEntity.status(HttpStatus.OK).body(categories);
        } catch (Exception e) {
            log.error("ROTA CATEGORY >>> Erro ao recuperar categorias", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // ROTA DE CRIAÇÃO DE NOVA CATEGORIA
    @PostMapping("/create")
    public ResponseEntity<Category> createCategory(@RequestBody CategoryDTO category) {
        log.info("ROTA CATEGORY >>> CRIAÇÃO DE CATEGORIA: {}", category);
        try {
            // Verifica se o merchantCode é válido
            if (!categoryService.isMerchantCodeValid(category.getMerchantCode())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            // Cria a nova categoria
            Category newCategory = categoryService.createCategory(category);
            return ResponseEntity.status(HttpStatus.CREATED).body(newCategory);
        } catch (Exception e) {
            log.error("ROTA CATEGORY >>> Erro ao criar categoria", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // ROTA PARA ATUALIZAR CATEGORIA
    @PutMapping("/update/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryDTO categoryDetails) {
        log.info("ROTA CATEGORY >>> ATUALIZAÇÃO DE CATEGORIA: {} - {}", id, categoryDetails.getMerchantCode());
        try {
            Category updatedCategory = categoryService.updateCategory(id, categoryDetails);
            if (updatedCategory == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.status(HttpStatus.OK).body(updatedCategory);
        } catch (Exception e) {
            log.error("ROTA CATEGORY >>> Erro ao atualizar categoria", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // ROTA PARA DELETAR CATEGORIA
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id,
            @RequestBody MerchantCodeDTO data) {
        log.info("ROTA CATEGORY >>> DELEÇÃO DE CATEGORIA: {} - {}", id, data.getMerchantCode());
        try {
            boolean deleted = categoryService.deleteCategory(id, data.getMerchantCode());
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            log.error("ROTA CATEGORY >>> Erro ao deletar categoria", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
