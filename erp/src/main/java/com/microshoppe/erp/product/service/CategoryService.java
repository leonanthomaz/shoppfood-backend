package com.microshoppe.erp.product.service;

import com.microshoppe.erp.authentication.repository.UserRepository;
import com.microshoppe.erp.product.dto.CategoryDTO;
import com.microshoppe.erp.product.model.Category;
import com.microshoppe.erp.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    private String generateRandomCategoryCode() {
        String uuid = UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "");
        return uuid.substring(0, Math.min(uuid.length(), 8)).toUpperCase();
    }

    public List<Category> findByNameContaining(String name) {
        return categoryRepository.findByNameContaining(name);
    }

    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> findAllCategoriesByMerchantCode(String merchantCode) {
        return categoryRepository.findByMerchantCode(merchantCode);
    }

    public Category createCategory(CategoryDTO category) {
        return categoryRepository.save(Category.builder()
                .name(category.getName())
                .description(category.getDescription())
                .merchantCode(category.getMerchantCode())
                .build());
    }

    public Category updateCategory(Long id, CategoryDTO categoryDetails) {
        return categoryRepository.findById(id).map(category -> {
            if (!category.getMerchantCode().equals(categoryDetails.getMerchantCode())) {
                throw new IllegalArgumentException("Merchant code mismatch");
            }
            category.setName(categoryDetails.getName());
            category.setDescription(categoryDetails.getDescription());
            return categoryRepository.save(category);
        }).orElse(null);
    }

    public boolean deleteCategory(Long id, String merchantCode) {
        return categoryRepository.findById(id).map(category -> {
            if (!category.getMerchantCode().equals(merchantCode)) {
                throw new IllegalArgumentException("Merchant code mismatch");
            }
            categoryRepository.deleteById(id);
            return true;
        }).orElse(false);
    }

    public Category findByIdAndMerchantCode(Long categoryId, String merchantCode) {
        return categoryRepository.findById(categoryId).map(category -> {
            if (!category.getMerchantCode().equals(merchantCode)) {
                throw new IllegalArgumentException("Merchant code mismatch");
            }
            return category;
        }).orElseThrow(() -> new IllegalArgumentException("Category not found"));
    }

    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    public boolean isMerchantCodeValid(String merchantCode) {
        return userRepository.existsByMerchantCode(merchantCode);
    }
}

